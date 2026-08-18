package com.gonaturefarms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "min_order", precision = 10, scale = 2)
    private BigDecimal minOrder;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- MANUAL GETTERS (in case Lombok fails) ---
    public String getCode() { return code; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinOrder() { return minOrder; }
    public DiscountType getDiscountType() { return discountType; }
    public Boolean getIsActive() { return isActive; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Integer getMaxUses() { return maxUses; }
    public Integer getUsedCount() { return usedCount; }

    // Additional setters for manual construction
    public void setCode(String code) { this.code = code; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public void setMinOrder(BigDecimal minOrder) { this.minOrder = minOrder; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public enum DiscountType {
        percent, flat
    }
}