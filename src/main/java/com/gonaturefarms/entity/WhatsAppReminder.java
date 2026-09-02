package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "whatsapp_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReminderType reminderType;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.Pending;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReminderType {
        Payment, Order, Delivery, Product, Custom
    }

    public enum ReminderStatus {
        Sent, Pending, Failed
    }

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public Long getAdminId() { return adminId; }
    public ReminderType getReminderType() { return reminderType; }
    public String getMessage() { return message; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public ReminderStatus getStatus() { return status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Manual setters as failsafe for Lombok processing issues
    public void setId(Long id) { this.id = id; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }
    public void setMessage(String message) { this.message = message; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public void setStatus(ReminderStatus status) { this.status = status; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static WhatsAppReminderBuilder builder() {
        return new WhatsAppReminderBuilder();
    }

    public static class WhatsAppReminderBuilder {
        private Long id;
        private Long adminId;
        private ReminderType reminderType;
        private String message;
        private LocalDateTime scheduledAt;
        private ReminderStatus status = ReminderStatus.Pending;
        private LocalDateTime sentAt;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        public WhatsAppReminderBuilder id(Long id) { this.id = id; return this; }
        public WhatsAppReminderBuilder adminId(Long adminId) { this.adminId = adminId; return this; }
        public WhatsAppReminderBuilder reminderType(ReminderType reminderType) { this.reminderType = reminderType; return this; }
        public WhatsAppReminderBuilder message(String message) { this.message = message; return this; }
        public WhatsAppReminderBuilder scheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public WhatsAppReminderBuilder status(ReminderStatus status) { this.status = status; return this; }
        public WhatsAppReminderBuilder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }
        public WhatsAppReminderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public WhatsAppReminderBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public WhatsAppReminder build() {
            WhatsAppReminder reminder = new WhatsAppReminder();
            reminder.id = this.id;
            reminder.adminId = this.adminId;
            reminder.reminderType = this.reminderType;
            reminder.message = this.message;
            reminder.scheduledAt = this.scheduledAt;
            reminder.status = this.status;
            reminder.sentAt = this.sentAt;
            reminder.createdAt = this.createdAt;
            reminder.updatedAt = this.updatedAt;
            return reminder;
        }
    }
}
