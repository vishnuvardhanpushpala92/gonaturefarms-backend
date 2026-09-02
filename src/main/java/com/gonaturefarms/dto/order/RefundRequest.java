package com.gonaturefarms.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private String orderId;
    private BigDecimal refundAmount;
    private String refundNotes;
    private String returnStatus; // "Approved", "Rejected", "PartialRefund"

    // Manual getters as failsafe for Lombok processing issues
    public String getOrderId() { return orderId; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public String getRefundNotes() { return refundNotes; }
    public String getReturnStatus() { return returnStatus; }

    // Manual setters as failsafe for Lombok processing issues
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public void setRefundNotes(String refundNotes) { this.refundNotes = refundNotes; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
}