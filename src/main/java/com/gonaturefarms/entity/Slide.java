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

/** Maps to the "slides" table (homepage hero carousel). */
@Entity
@Table(name = "slides")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Builder.Default
    @Column(length = 200)
    private String caption = "";

    @Builder.Default
    @Column(name = "sub_text", length = 200)
    private String subText = "";

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public String getSubText() { return subText; }
    public Integer getSortOrder() { return sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Additional setters for manual construction
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCaption(String caption) { this.caption = caption; }
    public void setSubText(String subText) { this.subText = subText; }

    // Static builder method as failsafe for Lombok @Builder
    public static SlideBuilder builder() {
        return new SlideBuilder();
    }

    public static class SlideBuilder {
        private Long id;
        private String imageUrl;
        private String caption = "";
        private String subText = "";
        private Integer sortOrder = 0;
        private LocalDateTime createdAt = LocalDateTime.now();

        public SlideBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SlideBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public SlideBuilder caption(String caption) {
            this.caption = caption;
            return this;
        }

        public SlideBuilder subText(String subText) {
            this.subText = subText;
            return this;
        }

        public SlideBuilder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public SlideBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Slide build() {
            Slide slide = new Slide();
            slide.id = this.id;
            slide.imageUrl = this.imageUrl;
            slide.caption = this.caption;
            slide.subText = this.subText;
            slide.sortOrder = this.sortOrder;
            slide.createdAt = this.createdAt;
            return slide;
        }
    }
}
