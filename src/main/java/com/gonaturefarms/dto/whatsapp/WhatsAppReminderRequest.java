package com.gonaturefarms.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WhatsAppReminderRequest {
    private String reminderType; // Changed to String to avoid enum validation issues

    @NotBlank(message = "Message is required")
    private String message;

    private LocalDateTime scheduledAt;

    private List<Long> customerIds;
}
