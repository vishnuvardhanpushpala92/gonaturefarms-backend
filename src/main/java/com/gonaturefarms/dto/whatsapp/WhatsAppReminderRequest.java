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

    // Manual getters as failsafe for Lombok processing issues
    public String getReminderType() { return reminderType; }
    public String getMessage() { return message; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public List<Long> getCustomerIds() { return customerIds; }

    // Manual setters as failsafe for Lombok processing issues
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }
    public void setMessage(String message) { this.message = message; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public void setCustomerIds(List<Long> customerIds) { this.customerIds = customerIds; }
}
