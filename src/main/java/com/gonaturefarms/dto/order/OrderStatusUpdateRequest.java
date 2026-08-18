package com.gonaturefarms.dto.order;

import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private String status;
    private String paymentStatus;
    private String trackingLocation;

    // Manual getters as failsafe for Lombok processing issues
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getTrackingLocation() { return trackingLocation; }
}
