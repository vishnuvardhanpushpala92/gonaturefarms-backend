package com.gonaturefarms.dto.coupon;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CouponValidateRequest {
    private String code;
    private BigDecimal orderTotal;

    // Manual getters as failsafe for Lombok processing issues
    public String getCode() { return code; }
    public BigDecimal getOrderTotal() { return orderTotal; }
}
