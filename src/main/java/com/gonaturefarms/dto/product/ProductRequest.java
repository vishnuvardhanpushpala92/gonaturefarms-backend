package com.gonaturefarms.dto.product;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
    private String additionalImages;

    @Valid
    private List<ProductVariantRequest> variants;

    // Manual getters as failsafe for Lombok processing issues
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getMrp() { return mrp; }
    public BigDecimal getGst() { return gst; }
    public String getHsn() { return hsn; }
    public String getCat() { return cat; }
    public String getImgUrl() { return imgUrl; }
    public String getStatus() { return status; }
    public String getAdditionalImages() { return additionalImages; }
    public List<ProductVariantRequest> getVariants() { return variants; }
}
