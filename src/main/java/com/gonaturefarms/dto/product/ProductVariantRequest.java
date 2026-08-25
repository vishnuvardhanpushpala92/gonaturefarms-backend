package com.gonaturefarms.dto.product;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductVariantRequest {
    @NotBlank(message = "Variant name is required")
    private String variantName;

    private BigDecimal mrp;

    // Manual getters as failsafe for Lombok processing issues
    public String getVariantName() { return variantName; }
    public BigDecimal getMrp() { return mrp; }
}
