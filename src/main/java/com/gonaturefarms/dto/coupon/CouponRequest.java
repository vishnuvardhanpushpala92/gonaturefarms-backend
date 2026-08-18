package com.gonaturefarms.dto.coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CouponRequest {
    private String code;
    private String description;
    private BigDecimal discountValue;
    private String discountType; // "percent" or "flat"
    private BigDecimal minOrder;
    private Integer maxUses;
    private LocalDateTime expiresAt;
    private Boolean isActive;

    // --- MANUAL GETTERS ---
    public String getCode() { return code; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinOrder() { return minOrder; }
    public String getDiscountType() { return discountType; }
    public Integer getMaxUses() { return maxUses; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Boolean getIsActive() { return isActive; }
}