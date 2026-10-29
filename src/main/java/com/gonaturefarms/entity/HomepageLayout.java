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

/**
 * Homepage layout configuration entity
 * Stores visual settings for hero slider, product display, and overall layout
 */
@Entity
@Table(name = "homepage_layout")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomepageLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hero Slider Settings
    @Column(name = "hero_height_desktop")
    @Builder.Default
    private Integer heroHeightDesktop = 450;

    @Column(name = "hero_height_tablet")
    @Builder.Default
    private Integer heroHeightTablet = 320;

    @Column(name = "hero_height_mobile")
    @Builder.Default
    private Integer heroHeightMobile = 250;

    @Column(name = "hero_image_fit")
    @Builder.Default
    private String heroImageFit = "contain"; // contain, cover

    @Column(name = "hero_image_position")
    @Builder.Default
    private String heroImagePosition = "center"; // center, top, bottom, left, right

    @Column(name = "hero_aspect_ratio")
    @Builder.Default
    private String heroAspectRatio = "16/9"; // 16/9, 16/7, 4/3, etc.

    @Column(name = "hero_background_color")
    @Builder.Default
    private String heroBackgroundColor = "#f5f5f0";

    // Product Display Settings
    @Column(name = "product_card_style")
    @Builder.Default
    private String productCardStyle = "standard"; // standard, rounded, bordered, shadow, compact

    @Column(name = "product_image_aspect_ratio")
    @Builder.Default
    private String productImageAspectRatio = "1/1"; // 1/1, 4/3, 16/9

    @Column(name = "product_image_fit")
    @Builder.Default
    private String productImageFit = "cover"; // contain, cover

    @Column(name = "products_per_row_desktop")
    @Builder.Default
    private Integer productsPerRowDesktop = 4;

    @Column(name = "products_per_row_tablet")
    @Builder.Default
    private Integer productsPerRowTablet = 3;

    @Column(name = "products_per_row_mobile")
    @Builder.Default
    private Integer productsPerRowMobile = 2;

    @Column(name = "product_card_spacing")
    @Builder.Default
    private Integer productCardSpacing = 16;

    // Layout Settings
    @Column(name = "section_spacing")
    @Builder.Default
    private Integer sectionSpacing = 48;

    @Column(name = "section_width")
    @Builder.Default
    private String sectionWidth = "full"; // full, contained, narrow

    @Column(name = "show_hero")
    @Builder.Default
    private Boolean showHero = true;

    @Column(name = "show_products")
    @Builder.Default
    private Boolean showProducts = true;

    @Column(name = "show_testimonials")
    @Builder.Default
    private Boolean showTestimonials = true;

    @Column(name = "show_videos")
    @Builder.Default
    private Boolean showVideos = true;

    @Column(name = "show_features")
    @Builder.Default
    private Boolean showFeatures = true;

    @Column(name = "product_section_order")
    @Builder.Default
    private Integer productSectionOrder = 1;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "is_draft")
    @Builder.Default
    private Boolean isDraft = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
