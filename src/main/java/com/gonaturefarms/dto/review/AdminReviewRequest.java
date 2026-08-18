package com.gonaturefarms.dto.review;

import lombok.Data;

@Data
public class AdminReviewRequest {
    private Long productId;
    private String userName;
    private Integer rating;
    private String comment;
    private Boolean featured;

    // Manual getters as failsafe for Lombok processing issues
    public Long getProductId() { return productId; }
    public String getUserName() { return userName; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public Boolean getFeatured() { return featured; }
}
