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

        Map<String, Object> cleaned = new LinkedHashMap<>();
        for (Map<String, Object> field : schema) {
            String key = String.valueOf(field.get("key"));
            boolean required = Boolean.TRUE.equals(field.get("required"));
            Object val = req.getFields().get(key);
            if (required && (val == null || String.valueOf(val).trim().isEmpty())) {
                throw new ApiException("\"" + field.get("label") + "\" is required");
            }
            if (val != null) {
                cleaned.put(key, clean(String.valueOf(val)));
            }
        }

        if (cleaned.isEmpty()) {
            throw new ApiException("Please fill in the form");
        }

        try {
            SupportTicket ticket = SupportTicket.builder()
                    .userId(req.getUserId())
                    .data(objectMapper.writeValueAsString(cleaned))
                    .status(SupportTicket.TicketStatus.open)
                    .build();
            ticketRepository.save(ticket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ApiResponse.ok("Your message has been sent! Our team will get back to you soon.");
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

    private int statusPriority(SupportTicket t) {
        return switch (t.getStatus()) {
            case open -> 0;
            case in_progress -> 1;
            case resolved -> 2;
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
