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
}