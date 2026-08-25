package com.gonaturefarms.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductVariantRequest {
    @NotBlank(message = "Variant name is required")
    private String variantName;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal mrp;

    @NotNull(message = "Stock is required")
    @Positive(message = "Stock must be positive")
    private Integer stock;

    // Manual getters as failsafe for Lombok processing issues
    public String getVariantName() { return variantName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getMrp() { return mrp; }
    public Integer getStock() { return stock; }
}
