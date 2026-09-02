package com.gonaturefarms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;

    @Column(nullable = false, length = 100)
    private String variantName;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal mrp = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 100;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getProductName() { return productName; }
    public String getVariantName() { return variantName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getMrp() { return mrp; }
    public Integer getStock() { return stock; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Manual setters as failsafe for Lombok processing issues
    public void setId(Long id) { this.id = id; }
    public void setProduct(Product product) { this.product = product; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static ProductVariantBuilder builder() {
        return new ProductVariantBuilder();
    }

    public static class ProductVariantBuilder {
        private Long id;
        private Product product;
        private String productName;
        private String variantName;
        private BigDecimal price = BigDecimal.ZERO;
        private BigDecimal mrp = BigDecimal.ZERO;
        private Integer stock = 100;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        public ProductVariantBuilder id(Long id) { this.id = id; return this; }
        public ProductVariantBuilder product(Product product) { this.product = product; return this; }
        public ProductVariantBuilder productName(String productName) { this.productName = productName; return this; }
        public ProductVariantBuilder variantName(String variantName) { this.variantName = variantName; return this; }
        public ProductVariantBuilder price(BigDecimal price) { this.price = price; return this; }
        public ProductVariantBuilder mrp(BigDecimal mrp) { this.mrp = mrp; return this; }
        public ProductVariantBuilder stock(Integer stock) { this.stock = stock; return this; }
        public ProductVariantBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ProductVariantBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ProductVariant build() {
            ProductVariant variant = new ProductVariant();
            variant.id = this.id;
            variant.product = this.product;
            variant.productName = this.productName;
            variant.variantName = this.variantName;
            variant.price = this.price;
            variant.mrp = this.mrp;
            variant.stock = this.stock;
            variant.createdAt = this.createdAt;
            variant.updatedAt = this.updatedAt;
            return variant;
        }
    }
}