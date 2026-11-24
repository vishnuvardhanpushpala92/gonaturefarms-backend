package com.gonaturefarms.dto.support;

import lombok.Data;

@Data
public class SupportTicketUpdateRequest {
    private String status;
    private String adminNote;
    private String customerResponse;

    // Manual getters as failsafe for Lombok processing issues
    public String getStatus() { return status; }
    public String getAdminNote() { return adminNote; }
    public String getCustomerResponse() { return customerResponse; }

    // Manual setters as failsafe for Lombok processing issues
    public void setStatus(String status) { this.status = status; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public void setCustomerResponse(String customerResponse) { this.customerResponse = customerResponse; }
}
