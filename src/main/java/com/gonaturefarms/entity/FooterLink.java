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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps to the "footer_links" table for dynamic footer navigation. */
@Entity
@Table(name = "footer_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FooterLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 50)
    private LinkCategory category = LinkCategory.QUICK_LINKS;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum LinkCategory {
        QUICK_LINKS,
        CUSTOMER_CARE
    }

    // Manual getters and setters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LinkCategory getCategory() { return category; }
    public void setCategory(LinkCategory category) { this.category = category; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static FooterLinkBuilder builder() {
        return new FooterLinkBuilder();
    }

    public static class FooterLinkBuilder {
        private Long id;
        private String name;
        private String url;
        private LinkCategory category = LinkCategory.QUICK_LINKS;
        private Integer sortOrder = 0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        public FooterLinkBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public FooterLinkBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FooterLinkBuilder url(String url) {
            this.url = url;
            return this;
        }

        public FooterLinkBuilder category(LinkCategory category) {
            this.category = category;
            return this;
        }

        public FooterLinkBuilder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public FooterLinkBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FooterLinkBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FooterLink build() {
            FooterLink footerLink = new FooterLink();
            footerLink.id = this.id;
            footerLink.name = this.name;
            footerLink.url = this.url;
            footerLink.category = this.category;
            footerLink.sortOrder = this.sortOrder;
            footerLink.createdAt = this.createdAt;
            footerLink.updatedAt = this.updatedAt;
            return footerLink;
        }
    }
}
