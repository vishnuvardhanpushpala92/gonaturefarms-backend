package com.gonaturefarms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @Column(name = "additional_images", columnDefinition = "TEXT")
    private String additionalImages; // JSON array of image URLs

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.current;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 100;

    @Builder.Default
    @Column(nullable = false)
    private Boolean pending = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private java.util.List<com.gonaturefarms.entity.ProductVariant> variants = new java.util.ArrayList<>();

    public enum ProductStatus {
        current, future
    }

    // Manual getters/setters as failsafe for Lombok processing issues
    public String getAdditionalImages() { return additionalImages; }
    public void setAdditionalImages(String additionalImages) { this.additionalImages = additionalImages; }

    // Static builder method as failsafe for Lombok @Builder
    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private BigDecimal mrp = BigDecimal.ZERO;
        private BigDecimal gst = BigDecimal.ZERO;
        private String hsn = "";
        private String cat = "";
        private String imgUrl;
        private String additionalImages;
        private ProductStatus status = ProductStatus.current;
        private Integer stock = 100;
        private Boolean pending = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private java.util.List<com.gonaturefarms.entity.ProductVariant> variants;

        public ProductBuilder id(Long id) { this.id = id; return this; }
        public ProductBuilder name(String name) { this.name = name; return this; }
        public ProductBuilder description(String description) { this.description = description; return this; }
        public ProductBuilder price(BigDecimal price) { this.price = price; return this; }
        public ProductBuilder mrp(BigDecimal mrp) { this.mrp = mrp; return this; }
        public ProductBuilder gst(BigDecimal gst) { this.gst = gst; return this; }
        public ProductBuilder hsn(String hsn) { this.hsn = hsn; return this; }
        public ProductBuilder cat(String cat) { this.cat = cat; return this; }
        public ProductBuilder imgUrl(String imgUrl) { this.imgUrl = imgUrl; return this; }
        public ProductBuilder additionalImages(String additionalImages) { this.additionalImages = additionalImages; return this; }
        public ProductBuilder status(ProductStatus status) { this.status = status; return this; }
        public ProductBuilder stock(Integer stock) { this.stock = stock; return this; }
        public ProductBuilder pending(Boolean pending) { this.pending = pending; return this; }
        public ProductBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ProductBuilder variants(java.util.List<com.gonaturefarms.entity.ProductVariant> variants) { this.variants = variants; return this; }

        public Product build() {
            Product product = new Product();
            product.id = this.id;
            product.name = this.name;
            product.description = this.description;
            product.price = this.price;
            product.mrp = this.mrp;
            product.gst = this.gst;
            product.hsn = this.hsn;
            product.cat = this.cat;
            product.imgUrl = this.imgUrl;
            product.additionalImages = this.additionalImages;
            product.status = this.status;
            product.stock = this.stock;
            product.pending = this.pending;
            product.createdAt = this.createdAt;
            product.variants = this.variants;
            return product;
        }
    }
}