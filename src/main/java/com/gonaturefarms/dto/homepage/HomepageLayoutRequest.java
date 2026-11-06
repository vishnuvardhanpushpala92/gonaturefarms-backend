package com.gonaturefarms.dto.homepage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Request DTO for homepage layout configuration
 */
@Data
public class HomepageLayoutRequest {

    // Hero Slider Settings
    @JsonProperty("heroHeightDesktop")
    private Integer heroHeightDesktop;

    @JsonProperty("heroHeightTablet")
    private Integer heroHeightTablet;

    @JsonProperty("heroHeightMobile")
    private Integer heroHeightMobile;

    @JsonProperty("heroImageFit")
    private String heroImageFit;

    @JsonProperty("heroImagePosition")
    private String heroImagePosition;

    @JsonProperty("heroAspectRatio")
    private String heroAspectRatio;

    @JsonProperty("heroBackgroundColor")
    private String heroBackgroundColor;

    // Product Display Settings
    @JsonProperty("productCardStyle")
    private String productCardStyle;

    @JsonProperty("productImageAspectRatio")
    private String productImageAspectRatio;

    @JsonProperty("productImageFit")
    private String productImageFit;

    @JsonProperty("productsPerRowDesktop")
    private Integer productsPerRowDesktop;

    @JsonProperty("productsPerRowTablet")
    private Integer productsPerRowTablet;

    @JsonProperty("productsPerRowMobile")
    private Integer productsPerRowMobile;

    @JsonProperty("productCardSpacing")
    private Integer productCardSpacing;

    // Layout Settings
    @JsonProperty("sectionSpacing")
    private Integer sectionSpacing;

    @JsonProperty("sectionWidth")
    private String sectionWidth;

    @JsonProperty("showHero")
    private Boolean showHero;

    @JsonProperty("showProducts")
    private Boolean showProducts;

    @JsonProperty("showTestimonials")
    private Boolean showTestimonials;

    @JsonProperty("showVideos")
    private Boolean showVideos;

    @JsonProperty("showFeatures")
    private Boolean showFeatures;

    @JsonProperty("productSectionOrder")
    private Integer productSectionOrder;

    // Publication control
    @JsonProperty("publish")
    private Boolean publish;

    @JsonProperty("saveAsDraft")
    private Boolean saveAsDraft;
}
