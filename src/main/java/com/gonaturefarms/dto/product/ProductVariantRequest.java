package com.gonaturefarms.dto.product;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductVariantRequest {
    @NotBlank(message = "Variant name is required")
    private String variantName;

    private BigDecimal price;
    
    private BigDecimal mrp;

    private Integer stock;

    // Manual getters as failsafe for Lombok processing issues
    public String getVariantName() { return variantName; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getMrp() { return mrp; }
    public Integer getStock() { return stock; }
}