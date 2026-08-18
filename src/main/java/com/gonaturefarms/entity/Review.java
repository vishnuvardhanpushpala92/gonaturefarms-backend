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

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public Short getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCustomerName() { return customerName; }
    public ReviewStatus getStatus() { return status; }
    public Boolean getFeatured() { return featured; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Additional setters for manual construction
    public void setStatus(ReviewStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static ReviewBuilder builder() {
        return new ReviewBuilder();
    }

    public static class ReviewBuilder {
        private Long id;
        private Long userId;
        private Long productId;
        private Short rating;
        private String comment;
        private String customerName = "";
        private ReviewStatus status = ReviewStatus.pending;
        private Boolean featured = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public ReviewBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ReviewBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ReviewBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public ReviewBuilder rating(Short rating) {
            this.rating = rating;
            return this;
        }

        public ReviewBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ReviewBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public ReviewBuilder status(ReviewStatus status) {
            this.status = status;
            return this;
        }

        public ReviewBuilder featured(Boolean featured) {
            this.featured = featured;
            return this;
        }

        public ReviewBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Review build() {
            Review review = new Review();
            review.id = this.id;
            review.userId = this.userId;
            review.productId = this.productId;
            review.rating = this.rating;
            review.comment = this.comment;
            review.customerName = this.customerName;
            review.status = this.status;
            review.featured = this.featured;
            review.createdAt = this.createdAt;
            return review;
        }
    }
}
