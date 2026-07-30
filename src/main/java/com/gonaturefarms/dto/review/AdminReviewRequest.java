package com.gonaturefarms.dto.review;

import lombok.Data;

@Data
public class AdminReviewRequest {
    private Long productId;
    private String userName;
    private Integer rating;
    private String comment;
    private Boolean featured;
}
