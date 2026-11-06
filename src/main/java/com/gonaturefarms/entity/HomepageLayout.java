package com.gonaturefarms.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("heroHeightDesktop")
    private Integer heroHeightDesktop = 450;

    @Column(name = "hero_height_tablet")
    @Builder.Default
    @JsonProperty("heroHeightTablet")
    private Integer heroHeightTablet = 320;

    @Column(name = "hero_height_mobile")
    @Builder.Default
    @JsonProperty("heroHeightMobile")
    private Integer heroHeightMobile = 250;

    @Column(name = "hero_image_fit")
    @Builder.Default
    @JsonProperty("heroImageFit")
    private String heroImageFit = "contain"; // contain, cover

    @Column(name = "hero_image_position")
    @Builder.Default
    @JsonProperty("heroImagePosition")
    private String heroImagePosition = "center"; // center, top, bottom, left, right

    @Column(name = "hero_aspect_ratio")
    @Builder.Default
    @JsonProperty("heroAspectRatio")
    private String heroAspectRatio = "16/9"; // 16/9, 16/7, 4/3, etc.

    @Column(name = "hero_background_color")
    @Builder.Default
    @JsonProperty("heroBackgroundColor")
    private String heroBackgroundColor = "#f5f5f0";

    // Product Display Settings
    @Column(name = "product_card_style")
    @Builder.Default
    @JsonProperty("productCardStyle")
    private String productCardStyle = "standard"; // standard, rounded, bordered, shadow, compact

    @Column(name = "product_image_aspect_ratio")
    @Builder.Default
    @JsonProperty("productImageAspectRatio")
    private String productImageAspectRatio = "1/1"; // 1/1, 4/3, 16/9

    @Column(name = "product_image_fit")
    @Builder.Default
    @JsonProperty("productImageFit")
    private String productImageFit = "cover"; // contain, cover

    @Column(name = "products_per_row_desktop")
    @Builder.Default
    @JsonProperty("productsPerRowDesktop")
    private Integer productsPerRowDesktop = 4;

    @Column(name = "products_per_row_tablet")
    @Builder.Default
    @JsonProperty("productsPerRowTablet")
    private Integer productsPerRowTablet = 3;

    @Column(name = "products_per_row_mobile")
    @Builder.Default
    @JsonProperty("productsPerRowMobile")
    private Integer productsPerRowMobile = 2;

    @Column(name = "product_card_spacing")
    @Builder.Default
    @JsonProperty("productCardSpacing")
    private Integer productCardSpacing = 16;

    // Layout Settings
    @Column(name = "section_spacing")
    @Builder.Default
    @JsonProperty("sectionSpacing")
    private Integer sectionSpacing = 48;

    @Column(name = "section_width")
    @Builder.Default
    @JsonProperty("sectionWidth")
    private String sectionWidth = "full"; // full, contained, narrow

    @Column(name = "show_hero")
    @Builder.Default
    @JsonProperty("showHero")
    private Boolean showHero = true;

    @Column(name = "show_products")
    @Builder.Default
    @JsonProperty("showProducts")
    private Boolean showProducts = true;

    @Column(name = "show_testimonials")
    @Builder.Default
    @JsonProperty("showTestimonials")
    private Boolean showTestimonials = true;

    @Column(name = "show_videos")
    @Builder.Default
    @JsonProperty("showVideos")
    private Boolean showVideos = true;

    @Column(name = "show_features")
    @Builder.Default
    @JsonProperty("showFeatures")
    private Boolean showFeatures = true;

    @Column(name = "product_section_order")
    @Builder.Default
    @JsonProperty("productSectionOrder")
    private Integer productSectionOrder = 1;

    @Column(name = "is_published")
    @Builder.Default
    @JsonProperty("isPublished")
    private Boolean isPublished = false;

    @Column(name = "is_draft")
    @Builder.Default
    @JsonProperty("isDraft")
    private Boolean isDraft = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    @JsonProperty("createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    @JsonProperty("createdBy")
    private String createdBy;

    @Column(name = "updated_by")
    @JsonProperty("updatedBy")
    private String updatedBy;
}
