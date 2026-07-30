package com.gonaturefarms.dto.admin;

import lombok.Data;

@Data
public class SlideRequest {
    private String imageUrl;
    private String caption;
    private String subText;
    private Integer sortOrder;
}
