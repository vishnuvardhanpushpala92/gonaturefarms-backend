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

/** Maps to the "coupons" table. */
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

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType = DiscountType.flat;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Builder.Default
    @Column(name = "min_order", precision = 10, scale = 2)
    private BigDecimal minOrder = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "max_uses")
    private Integer maxUses = 9999;

    @Builder.Default
    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DiscountType {
        flat, percent
    }
}
