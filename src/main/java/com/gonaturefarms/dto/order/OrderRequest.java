package com.gonaturefarms.dto.order;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

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

    // Manual getters as failsafe for Lombok processing issues
    public Long getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getArea() { return area; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentUtr() { return paymentUtr; }
    public String getPaymentScreenshotUrl() { return paymentScreenshotUrl; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getGstAmount() { return gstAmount; }
    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotal() { return total; }
    public String getCouponCode() { return couponCode; }
    public List<OrderItemRequest> getItems() { return items; }
}
