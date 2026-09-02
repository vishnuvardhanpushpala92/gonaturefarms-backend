package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps to the "support_tickets" table.
 * The original MySQL column was a JSON type holding an arbitrary object whose shape
 * is defined at runtime by the admin-configurable "support_fields" site setting.
 * We store it as TEXT (serialized JSON string) for maximum portability across
 * PostgreSQL versions; PostgreSQL's native JSON/JSONB type could be used instead
 * if stronger query support over the ticket contents is required later.
 */
@Entity
@Table(name = "support_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String data;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.open;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum TicketStatus {
        open, in_progress, resolved
    }

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getData() { return data; }
    public TicketStatus getStatus() { return status; }
    public String getAdminNote() { return adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Manual setters as failsafe for Lombok processing issues
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setData(String data) { this.data = data; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static SupportTicketBuilder builder() {
        return new SupportTicketBuilder();
    }

    public static class SupportTicketBuilder {
        private Long id;
        private Long userId;
        private String data;
        private TicketStatus status = TicketStatus.open;
        private String adminNote;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public SupportTicketBuilder id(Long id) { this.id = id; return this; }
        public SupportTicketBuilder userId(Long userId) { this.userId = userId; return this; }
        public SupportTicketBuilder data(String data) { this.data = data; return this; }
        public SupportTicketBuilder status(TicketStatus status) { this.status = status; return this; }
        public SupportTicketBuilder adminNote(String adminNote) { this.adminNote = adminNote; return this; }
        public SupportTicketBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SupportTicketBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public SupportTicket build() {
            SupportTicket ticket = new SupportTicket();
            ticket.id = this.id;
            ticket.userId = this.userId;
            ticket.data = this.data;
            ticket.status = this.status;
            ticket.adminNote = this.adminNote;
            ticket.createdAt = this.createdAt;
            ticket.updatedAt = this.updatedAt;
            return ticket;
        }
    }
}
