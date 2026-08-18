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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps to the "products" table. */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal mrp = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal gst = BigDecimal.ZERO;

    @Builder.Default
    @Column(length = 20)
    private String hsn = "";

    @Builder.Default
    @Column(length = 80)
    private String cat = "";

    @Column(name = "img_url", columnDefinition = "TEXT")
    private String imgUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.current;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private java.util.List<com.gonaturefarms.entity.ProductVariant> variants;

    public enum ProductStatus {
        current, future
    }
}
