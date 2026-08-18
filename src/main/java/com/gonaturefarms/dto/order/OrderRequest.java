package com.gonaturefarms.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    private String area;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private String paymentMethod;

    private String paymentUtr;

    private String paymentScreenshotUrl;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal discount;
    private BigDecimal total;
    private String couponCode;
    private Long userId;
}
