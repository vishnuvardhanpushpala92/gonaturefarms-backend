package com.gonaturefarms.dto.review;

import lombok.Data;

@Data
public class ReviewRequest {
    private Integer rating;
    private String comment;
}
