package com.gonaturefarms.dto.review;

import lombok.Data;

@Data
public class FeatureToggleRequest {
    private Boolean featured;

    // Manual getters as failsafe for Lombok processing issues
    public Boolean getFeatured() { return featured; }

    // Manual setters as failsafe for Lombok processing issues
    public void setFeatured(Boolean featured) { this.featured = featured; }
}
