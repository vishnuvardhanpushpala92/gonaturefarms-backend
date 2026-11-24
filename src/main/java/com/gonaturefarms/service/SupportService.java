package com.gonaturefarms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.support.SupportTicketRequest;
import com.gonaturefarms.dto.support.SupportTicketUpdateRequest;
import com.gonaturefarms.entity.SiteSetting;
import com.gonaturefarms.entity.SupportTicket;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.SiteSettingRepository;
import com.gonaturefarms.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic for customer support tickets, whose field schema is defined at
 * runtime by the admin-configurable "support_fields" site setting. Mirrors routes/support.js.
 */
@Service
public class SupportService {

    private final SupportTicketRepository ticketRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final ObjectMapper objectMapper;

    public SupportService(SupportTicketRepository ticketRepository, SiteSettingRepository siteSettingRepository,
                           ObjectMapper objectMapper) {
        this.ticketRepository = ticketRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApiResponse submit(SupportTicketRequest req) {
        if (req.getFields() == null || req.getFields().isEmpty()) {
            throw new ApiException("Invalid submission");
        }

        String schemaJson = siteSettingRepository.findByKey("support_fields").map(SiteSetting::getValue).orElse("[]");
        List<Map<String, Object>> schema;
        try {
            schema = objectMapper.readValue(schemaJson, List.class);
        } catch (Exception e) {
            schema = List.of();
        }

        Map<String, Object> fields = req.getFields();

        // Complaint fields are whitelisted in addition to the configurable form schema.
        String[] complaintKeys = {"full_name", "name", "mobile", "phone", "email", "order_id",
                "category", "subject", "description", "message", "images"};
        Map<String, Object> cleaned = new LinkedHashMap<>();
        for (Map<String, Object> field : schema) {
            String key = String.valueOf(field.get("key"));
            boolean required = Boolean.TRUE.equals(field.get("required"));
            Object val = fields.get(key);
            if (required && (val == null || String.valueOf(val).trim().isEmpty())) {
                throw new ApiException("\"" + field.get("label") + "\" is required");
            }
            if (val != null) {
                cleaned.put(key, clean(String.valueOf(val)));
            }
        }
        for (String key : complaintKeys) {
            Object val = fields.get(key);
            if (val == null) continue;
            if ("images".equals(key) && val instanceof java.util.List<?> list) {
                List<String> urls = list.stream().map(String::valueOf).limit(5).toList();
                if (!urls.isEmpty()) cleaned.put("images", urls);
            } else {
                cleaned.put(key, clean(String.valueOf(val)));
            }
        }

        // Complaint validation: name + mobile + description are required for the complaint flow.
        String name = firstNonBlank(fields, "full_name", "name");
        String mobile = firstNonBlank(fields, "mobile", "phone");
        String description = firstNonBlank(fields, "description", "message");
        boolean isComplaint = fields.containsKey("category") || fields.containsKey("subject")
                || fields.containsKey("order_id") || fields.containsKey("images");
        if (isComplaint) {
            if (name == null) throw new ApiException("Full name is required");
            if (mobile == null || !mobile.replaceAll("\\D", "").matches("\\d{10,15}"))
                throw new ApiException("A valid mobile number is required");
            if (description == null) throw new ApiException("Complaint description is required");
        }

        if (cleaned.isEmpty()) {
            throw new ApiException("Please fill in the form");
        }

        try {
            SupportTicket ticket = SupportTicket.builder()
                    .userId(req.getUserId())
                    .ticketNumber(generateTicketNumber())
                    .category(str(fields.get("category")))
                    .orderId(toLong(fields.get("order_id")))
                    .customerName(name)
                    .customerMobile(mobile != null ? mobile.replaceAll("\\D", "") : null)
                    .customerEmail(str(fields.get("email")))
                    .data(objectMapper.writeValueAsString(cleaned))
                    .status(SupportTicket.TicketStatus.open)
                    .build();
            ticketRepository.save(ticket);
            return ApiResponse.ok("Complaint submitted successfully")
                    .with("ticket_number", ticket.getTicketNumber())
                    .with("ticket_id", ticket.getId());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Public complaint tracking: reference number + registered mobile must match. */
    @Transactional(readOnly = true)
    public ApiResponse trackComplaint(String ref, String mobile) {
        if (ref == null || ref.isBlank() || mobile == null || mobile.isBlank()) {
            throw new ApiException("Reference number and mobile number are required");
        }
        String digits = mobile.replaceAll("\\D", "");
        SupportTicket ticket = ticketRepository.findByTicketNumber(ref.trim())
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Complaint not found"));
        String tm = ticket.getCustomerMobile() != null ? ticket.getCustomerMobile().replaceAll("\\D", "") : null;
        if (tm == null || !tm.equals(digits)) {
            throw new ApiException("Mobile number does not match this complaint");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ticket_number", ticket.getTicketNumber());
        map.put("status", ticket.getStatus());
        map.put("category", ticket.getCategory());
        map.put("customer_response", ticket.getCustomerResponse());
        map.put("created_at", ticket.getCreatedAt());
        map.put("updated_at", ticket.getUpdatedAt());
        return ApiResponse.ok().with("complaint", map);
    }

    private String generateTicketNumber() {
        return "CMP-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String firstNonBlank(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).trim().isEmpty()) return String.valueOf(v).trim();
        }
        return null;
    }

    private String str(Object v) {
        return v == null || String.valueOf(v).trim().isEmpty() ? null : clean(String.valueOf(v).trim());
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try { return Long.parseLong(String.valueOf(v).trim()); } catch (Exception e) { return null; }
    }

    @Transactional(readOnly = true)
    public ApiResponse adminAll() {
        List<SupportTicket> tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        // Mirrors: ORDER BY (status='open') DESC, (status='in_progress') DESC, created_at DESC
        List<Map<String, Object>> result = tickets.stream()
                .sorted(Comparator.comparingInt(this::statusPriority))
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.ok().with("tickets", result);
    }

    @Transactional(readOnly = true)
    public ApiResponse getTicketsByUserId(Long userId) {
        List<SupportTicket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = tickets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.ok().with("tickets", result);
    }

    @Transactional(readOnly = true)
    public ApiResponse getTicketById(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Ticket not found"));
        return ApiResponse.ok().with("ticket", toDto(ticket));
    }

    @Transactional(readOnly = true)
    public ApiResponse getTicketByIdAndUserId(Long id, Long userId) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Ticket not found"));
        if (!ticket.getUserId().equals(userId)) {
            throw new com.gonaturefarms.exception.ApiException("Access denied");
        }
        return ApiResponse.ok().with("ticket", toDto(ticket));
    }

    private int statusPriority(SupportTicket t) {
        return switch (t.getStatus()) {
            case open -> 0;
            case pending -> 1;
            case in_progress -> 2;
            case resolved -> 3;
            case rejected -> 4;
        };
    }

    private Map<String, Object> toDto(SupportTicket t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("user_id", t.getUserId());
        try {
            map.put("data", objectMapper.readValue(t.getData(), Map.class));
        } catch (Exception e) {
            map.put("data", t.getData());
        }
        map.put("status", t.getStatus());
        map.put("admin_note", t.getAdminNote());
        map.put("customer_name", t.getCustomerName());
        map.put("customer_mobile", t.getCustomerMobile());
        map.put("customer_email", t.getCustomerEmail());
        map.put("customer_response", t.getCustomerResponse());
        map.put("ticket_number", t.getTicketNumber());
        map.put("category", t.getCategory());
        map.put("order_id", t.getOrderId());
        map.put("created_at", t.getCreatedAt());
        map.put("updated_at", t.getUpdatedAt());
        return map;
    }

    @Transactional
    public ApiResponse update(Long id, SupportTicketUpdateRequest req) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Ticket not found"));
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            ticket.setStatus(SupportTicket.TicketStatus.valueOf(req.getStatus()));
        }
        if (req.getAdminNote() != null) {
            ticket.setAdminNote(req.getAdminNote());
        }
        if (req.getCustomerResponse() != null) {
            ticket.setCustomerResponse(req.getCustomerResponse());
        }
        ticket.setUpdatedAt(java.time.LocalDateTime.now());
        ticketRepository.save(ticket);
        return ApiResponse.ok("Ticket updated");
    }

    @Transactional
    public ApiResponse delete(Long id) {
        ticketRepository.deleteById(id);
        return ApiResponse.ok("Ticket deleted");
    }

    /** Basic HTML-escaping to avoid stored XSS, mirroring clean() in routes/support.js. */
    private String clean(String v) {
        if (v.length() > 3000) v = v.substring(0, 3000);
        return v.replaceAll("[<>]", "");
    }
}
