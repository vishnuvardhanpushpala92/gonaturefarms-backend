package com.gonaturefarms.dto.order;

import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    private String status;
    private String paymentStatus;
    private String trackingLocation;
}
