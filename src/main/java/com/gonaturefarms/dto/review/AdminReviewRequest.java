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
}