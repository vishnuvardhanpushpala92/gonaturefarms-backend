package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps to the "reviews" table. Unique constraint on (user_id, product_id). */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_product_review", columnNames = {"user_id", "product_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Short rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Builder.Default
    @Column(name = "customer_name", length = 120)
    private String customerName = "";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.pending;

    @Builder.Default
    @Column(nullable = false)
    private Boolean featured = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ReviewStatus {
        pending, approved
    }
}
