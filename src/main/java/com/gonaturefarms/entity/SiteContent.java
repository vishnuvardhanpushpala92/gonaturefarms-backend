package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps to the "site_content" table for dynamic site pages like About Us. */
@Entity
@Table(name = "site_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "person_name", length = 200)
    private String personName;

    @Column(name = "person_role", length = 200)
    private String personRole;

    @Column(name = "person_image_url", columnDefinition = "TEXT")
    private String personImageUrl;

    @Column(name = "optional_link", columnDefinition = "TEXT")
    private String optionalLink;

    @Builder.Default
    @Column
    private Boolean pending = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Manual getters and setters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getPersonRole() { return personRole; }
    public void setPersonRole(String personRole) { this.personRole = personRole; }

    public String getPersonImageUrl() { return personImageUrl; }
    public void setPersonImageUrl(String personImageUrl) { this.personImageUrl = personImageUrl; }

    public String getOptionalLink() { return optionalLink; }
    public void setOptionalLink(String optionalLink) { this.optionalLink = optionalLink; }

    public Boolean getPending() { return pending; }
    public void setPending(Boolean pending) { this.pending = pending; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static builder method as failsafe for Lombok @Builder
    public static SiteContentBuilder builder() {
        return new SiteContentBuilder();
    }

    public static class SiteContentBuilder {
        private Long id;
        private String slug;
        private String title;
        private String description;
        private String imageUrl;
        private String personName;
        private String personRole;
        private String personImageUrl;
        private String optionalLink;
        private Boolean pending = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        public SiteContentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SiteContentBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public SiteContentBuilder title(String title) {
            this.title = title;
            return this;
        }

        public SiteContentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public SiteContentBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public SiteContentBuilder personName(String personName) {
            this.personName = personName;
            return this;
        }

        public SiteContentBuilder personRole(String personRole) {
            this.personRole = personRole;
            return this;
        }

        public SiteContentBuilder personImageUrl(String personImageUrl) {
            this.personImageUrl = personImageUrl;
            return this;
        }

        public SiteContentBuilder optionalLink(String optionalLink) {
            this.optionalLink = optionalLink;
            return this;
        }

        public SiteContentBuilder pending(Boolean pending) {
            this.pending = pending;
            return this;
        }

        public SiteContentBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SiteContentBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public SiteContent build() {
            SiteContent siteContent = new SiteContent();
            siteContent.id = this.id;
            siteContent.slug = this.slug;
            siteContent.title = this.title;
            siteContent.description = this.description;
            siteContent.imageUrl = this.imageUrl;
            siteContent.personName = this.personName;
            siteContent.personRole = this.personRole;
            siteContent.personImageUrl = this.personImageUrl;
            siteContent.optionalLink = this.optionalLink;
            siteContent.pending = this.pending;
            siteContent.createdAt = this.createdAt;
            siteContent.updatedAt = this.updatedAt;
            return siteContent;
        }
    }
}
