package com.gonaturefarms.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminReviewRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Rating is required")
    private Integer rating;

    private String comment;
    private Boolean featured;

    // ✅ FIX: Added "userName" field to match old ReviewService.java
    private String userName;

    // Manual getters as failsafe for Lombok processing issues
    public Long getProductId() { return productId; }
    public String getCustomerName() { return customerName; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public Boolean getFeatured() { return featured; }

    // ✅ FIX: Added getter for userName so ReviewService compiles
    public String getUserName() { return userName; }
}