package com.gonaturefarms.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WhatsAppReminderRequest {
    @NotNull(message = "Reminder type is required")
    private com.gonaturefarms.entity.WhatsAppReminder.ReminderType reminderType;

    @NotBlank(message = "Message is required")
    private String message;

    private LocalDateTime scheduledAt;

    private List<Long> customerIds;
}
