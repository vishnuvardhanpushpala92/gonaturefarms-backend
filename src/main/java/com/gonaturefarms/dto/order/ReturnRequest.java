package com.gonaturefarms.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {
    private String orderId;
    private String reason;
    private String notes;
    private String returnStatus;

    // Manual getters as failsafe for Lombok processing issues
    public String getOrderId() { return orderId; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
    public String getReturnStatus() { return returnStatus; }

    // Manual setters as failsafe for Lombok processing issues
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setReason(String reason) { this.reason = reason; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
}