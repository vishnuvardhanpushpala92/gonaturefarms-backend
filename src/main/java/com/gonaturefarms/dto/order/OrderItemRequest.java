package com.gonaturefarms.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

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
}
