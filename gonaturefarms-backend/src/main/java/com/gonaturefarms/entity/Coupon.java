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

    // Static builder method as failsafe for Lombok @Builder
    public static CouponBuilder builder() {
        return new CouponBuilder();
    }

    public static class CouponBuilder {
        private Long id;
        private String code;
        private String description;
        private BigDecimal discountValue;
        private DiscountType discountType;
        private BigDecimal minOrder;
        private Integer maxUses;
        private LocalDateTime expiresAt;
        private Boolean isActive = true;
        private Integer usedCount = 0;
        private LocalDateTime createdAt = LocalDateTime.now();

        public CouponBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CouponBuilder code(String code) {
            this.code = code;
            return this;
        }

        public CouponBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CouponBuilder discountValue(BigDecimal discountValue) {
            this.discountValue = discountValue;
            return this;
        }

        public CouponBuilder discountType(DiscountType discountType) {
            this.discountType = discountType;
            return this;
        }

        public CouponBuilder minOrder(BigDecimal minOrder) {
            this.minOrder = minOrder;
            return this;
        }

        public CouponBuilder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        public CouponBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public CouponBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public CouponBuilder usedCount(Integer usedCount) {
            this.usedCount = usedCount;
            return this;
        }

        public CouponBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Coupon build() {
            Coupon coupon = new Coupon();
            coupon.id = this.id;
            coupon.code = this.code;
            coupon.description = this.description;
            coupon.discountValue = this.discountValue;
            coupon.discountType = this.discountType;
            coupon.minOrder = this.minOrder;
            coupon.maxUses = this.maxUses;
            coupon.expiresAt = this.expiresAt;
            coupon.isActive = this.isActive;
            coupon.usedCount = this.usedCount;
            coupon.createdAt = this.createdAt;
            return coupon;
        }
    }

    public enum DiscountType {
        percent, flat
    }
}