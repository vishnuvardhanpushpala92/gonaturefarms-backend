package com.gonaturefarms.dto.review;

import lombok.Data;

@Data
public class ReviewRequest {
    private Integer rating;
    private String comment;

    // Manual getters as failsafe for Lombok processing issues
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
}
