package com.gonaturefarms.dto.order;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Mirrors a single cart item sent by the frontend when placing an order:
 * { id, name, img, price, gst, qty }
 */
@Data
public class OrderItemRequest {
    private Long id;

    @NotBlank(message = "Item name is required")
    private String name;

    private String img;

    @NotNull(message = "Item price is required")
    private BigDecimal price;

    private BigDecimal gst;

    @NotNull(message = "Item quantity is required")
    @Positive(message = "Item quantity must be positive")
    private Integer qty;

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getImg() { return img; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getGst() { return gst; }
    public Integer getQty() { return qty; }
}
