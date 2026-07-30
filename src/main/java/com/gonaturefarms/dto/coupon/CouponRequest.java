package com.gonaturefarms.dto.coupon;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrder;
    private Integer maxUses;
    private LocalDateTime expiresAt;
}
