package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.whatsapp.WhatsAppReminderRequest;
import com.gonaturefarms.entity.User;
import com.gonaturefarms.entity.WhatsAppReminder;
import com.gonaturefarms.repository.UserRepository;
import com.gonaturefarms.repository.WhatsAppReminderRepository;
import com.gonaturefarms.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class WhatsAppReminderService {

    private static final Logger logger = Logger.getLogger(WhatsAppReminderService.class.getName());

    private final WhatsAppReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public WhatsAppReminderService(WhatsAppReminderRepository reminderRepository, UserRepository userRepository) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse getAllReminders(Long adminId) {
        List<WhatsAppReminder> reminders = reminderRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
        return ApiResponse.ok().with("reminders", reminders);
    }

    @Transactional
    public ApiResponse createReminder(Long adminId, WhatsAppReminderRequest request) {
        // Convert String reminderType to enum
        WhatsAppReminder.ReminderType reminderType;
        try {
            reminderType = WhatsAppReminder.ReminderType.valueOf(request.getReminderType());
        } catch (Exception e) {
            reminderType = WhatsAppReminder.ReminderType.Custom; // Default to Custom if invalid
        }

        WhatsAppReminder reminder = WhatsAppReminder.builder()
                .adminId(adminId)
                .reminderType(reminderType)
                .message(request.getMessage())
                .scheduledAt(request.getScheduledAt())
                .status(WhatsAppReminder.ReminderStatus.Pending)
                .build();

        reminder = reminderRepository.save(reminder);
        return ApiResponse.ok("Reminder created successfully").with("reminder", reminder);
    }

    @Transactional
    public ApiResponse sendReminder(Long reminderId) {
        WhatsAppReminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Reminder not found"));

        // Mock implementation - log the message instead of sending real WhatsApp
        logger.info("MOCK WhatsApp Message: " + reminder.getMessage());
        logger.info("Scheduled for: " + reminder.getScheduledAt());

        reminder.setStatus(WhatsAppReminder.ReminderStatus.Sent);
        reminder.setSentAt(LocalDateTime.now());
        reminderRepository.save(reminder);

        return ApiResponse.ok("Reminder sent successfully (mock implementation)");
    }

    @Transactional
    public ApiResponse sendToCustomers(Long adminId, WhatsAppReminderRequest request) {
        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            throw new com.gonaturefarms.exception.ApiException("Customer IDs are required");
        }

        // Convert String reminderType to enum
        WhatsAppReminder.ReminderType reminderType;
        try {
            reminderType = WhatsAppReminder.ReminderType.valueOf(request.getReminderType());
        } catch (Exception e) {
            reminderType = WhatsAppReminder.ReminderType.Custom; // Default to Custom if invalid
        }

        java.util.List<String> whatsappLinks = new java.util.ArrayList<>();

        for (Long customerId : request.getCustomerIds()) {
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Customer not found"));

            // Check if customer has opted out
            if (Boolean.TRUE.equals(customer.getWhatsappOptOut())) {
                logger.info("Customer " + customer.getName() + " has opted out of WhatsApp reminders");
                continue;
            }

            // Normalize phone number for WhatsApp (add 91 for Indian numbers if not present)
            String phoneNumber = customer.getPhone();
            if (phoneNumber != null && !phoneNumber.startsWith("91") && phoneNumber.length() == 10) {
                phoneNumber = "91" + phoneNumber;
            }

            // Create WhatsApp click-to-chat link
            String encodedMessage = java.net.URLEncoder.encode(request.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            String whatsappLink = "https://wa.me/" + phoneNumber + "?text=" + encodedMessage;
            whatsappLinks.add(whatsappLink);

            logger.info("WhatsApp link generated for " + customer.getName() + " (" + phoneNumber + ")");
        }

        // Create a reminder record
        WhatsAppReminder reminder = WhatsAppReminder.builder()
                .adminId(adminId)
                .reminderType(reminderType)
                .message(request.getMessage())
                .scheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now())
                .status(WhatsAppReminder.ReminderStatus.Sent)
                .sentAt(LocalDateTime.now())
                .build();

        reminderRepository.save(reminder);

        return ApiResponse.ok("WhatsApp links generated for " + whatsappLinks.size() + " customers")
                .with("whatsappLinks", whatsappLinks);
    }

    @Transactional
    public ApiResponse deleteReminder(Long reminderId) {
        reminderRepository.findById(reminderId)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Reminder not found"));
        reminderRepository.deleteById(reminderId);
        return ApiResponse.ok("Reminder deleted successfully");
    }

    @Transactional
    public ApiResponse generateProductReminder(Long adminId, Long productId, String productName) {
        // Generate reminders for all customers who haven't opted out
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.customer)
                .filter(u -> !Boolean.TRUE.equals(u.getWhatsappOptOut()))
                .toList();

        for (User customer : customers) {
            WhatsAppReminder reminder = WhatsAppReminder.builder()
                    .adminId(adminId)
                    .reminderType(WhatsAppReminder.ReminderType.Product)
                    .message("New product available: " + productName + ". Check it out on Go Nature Farms!")
                    .scheduledAt(LocalDateTime.now())
                    .status(WhatsAppReminder.ReminderStatus.Pending)
                    .build();
            reminderRepository.save(reminder);
        }

        return ApiResponse.ok("Generated " + customers.size() + " product reminders for customers");
    }
}