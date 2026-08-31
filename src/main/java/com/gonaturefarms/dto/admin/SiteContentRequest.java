package com.gonaturefarms.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SiteContentRequest {
    private String slug;
    private String title;
    private String description;
    private String imageUrl;
    private String personName;
    private String personRole;
    private String personImageUrl;

    // Manual getters as failsafe for Lombok processing issues
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getPersonName() { return personName; }
    public String getPersonRole() { return personRole; }
    public String getPersonImageUrl() { return personImageUrl; }
}
