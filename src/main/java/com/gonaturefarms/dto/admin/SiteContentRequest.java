package com.gonaturefarms.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SiteContentRequest {
    @JsonProperty("slug")
    private String slug;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("image_url")
    private String imageUrl;
    
    @JsonProperty("person_name")
    private String personName;
    
    @JsonProperty("person_role")
    private String personRole;
    
    @JsonProperty("person_image_url")
    private String personImageUrl;
    
    @JsonProperty("optional_link")
    private String optionalLink;

    // Manual getters as failsafe for Lombok processing issues
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getPersonName() { return personName; }
    public String getPersonRole() { return personRole; }
    public String getPersonImageUrl() { return personImageUrl; }
    public String getOptionalLink() { return optionalLink; }
}
