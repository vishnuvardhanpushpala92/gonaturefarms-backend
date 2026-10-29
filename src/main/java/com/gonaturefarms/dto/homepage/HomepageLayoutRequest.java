package com.gonaturefarms.dto.homepage;

import lombok.Data;

/**
 * Request DTO for homepage layout configuration
 */
@Data
public class HomepageLayoutRequest {

    // Hero Slider Settings
    private Integer heroHeightDesktop;
    private Integer heroHeightTablet;
    private Integer heroHeightMobile;
    private String heroImageFit;
    private String heroImagePosition;
    private String heroAspectRatio;
    private String heroBackgroundColor;

    // Product Display Settings
    private String productCardStyle;
    private String productImageAspectRatio;
    private String productImageFit;
    private Integer productsPerRowDesktop;
    private Integer productsPerRowTablet;
    private Integer productsPerRowMobile;
    private Integer productCardSpacing;

    // Layout Settings
    private Integer sectionSpacing;
    private String sectionWidth;
    private Boolean showHero;
    private Boolean showProducts;
    private Boolean showTestimonials;
    private Boolean showVideos;
    private Boolean showFeatures;
    private Integer productSectionOrder;

    // Publication control
    private Boolean publish;
    private Boolean saveAsDraft;
}
