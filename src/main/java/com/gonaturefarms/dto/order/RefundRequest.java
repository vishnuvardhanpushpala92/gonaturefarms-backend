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
}