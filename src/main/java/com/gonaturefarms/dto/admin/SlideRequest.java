package com.gonaturefarms.dto.admin;

import lombok.Data;

@Data
public class SlideRequest {
    private String imageUrl;
    private String caption;
    private String subText;
    private Integer sortOrder;

    // Manual getters as failsafe for Lombok processing issues
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public String getSubText() { return subText; }
    public Integer getSortOrder() { return sortOrder; }
}
