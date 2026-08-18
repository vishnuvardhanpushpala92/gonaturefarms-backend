package com.gonaturefarms.dto.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private BigDecimal mrp;
    private BigDecimal gst;
    private String hsn;
    private String cat;
    private String imgUrl;
    private String status;

    @Valid
    private List<ProductVariantRequest> variants;
}
