package com.gonaturefarms.dto.coupon;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponValidateRequest {
    private String code;
    private BigDecimal orderTotal;
}
