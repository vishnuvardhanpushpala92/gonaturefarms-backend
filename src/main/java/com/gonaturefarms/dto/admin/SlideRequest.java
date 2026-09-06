package com.gonaturefarms.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SlideRequest {
    @JsonProperty("image_url")
    private String imageUrl;
    
    private String caption;
    
    @JsonProperty("sub_text")
    private String subText;
    
    @JsonProperty("sort_order")
    private Integer sortOrder;

    // Manual getters as failsafe for Lombok processing issues
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public String getSubText() { return subText; }
    public Integer getSortOrder() { return sortOrder; }
}
