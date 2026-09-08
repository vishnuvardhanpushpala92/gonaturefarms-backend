package com.gonaturefarms.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gonaturefarms.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {
    private Long id;
    private String orderId;
    private Long userId;
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private String area;
    private String city;
    private String state;
    private String pincode;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal discount;
    private BigDecimal total;
    private Order.OrderStatus status;
    private Order.PaymentStatus paymentStatus;
    private String trackingLocation;
    private String notes;
    private String paymentUtr;
    private String paymentScreenshotUrl;
    private Boolean paymentVerified;
    private Boolean returnRequested;
    private String returnReason;
    private LocalDateTime returnRequestedAt;
    private String returnStatus;
    private LocalDateTime returnProcessedAt;
    private BigDecimal refundAmount;
    private String refundNotes;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public OrderDTO() {}

    public static OrderDTO fromEntity(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setCustomerName(order.getCustomerName());
        dto.setPhone(order.getPhone());
        dto.setEmail(order.getEmail());
        dto.setAddress(order.getAddress());
        dto.setArea(order.getArea());
        dto.setCity(order.getCity());
        dto.setState(order.getState());
        dto.setPincode(order.getPincode());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setSubtotal(order.getSubtotal());
        dto.setGstAmount(order.getGstAmount());
        dto.setDeliveryCharge(order.getDeliveryCharge());
        dto.setDiscount(order.getDiscount());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTrackingLocation(order.getTrackingLocation());
        dto.setNotes(order.getNotes());
        dto.setPaymentUtr(order.getPaymentUtr());
        dto.setPaymentScreenshotUrl(order.getPaymentScreenshotUrl());
        dto.setPaymentVerified(order.getPaymentVerified());
        dto.setReturnRequested(order.getReturnRequested());
        dto.setReturnReason(order.getReturnReason());
        dto.setReturnRequestedAt(order.getReturnRequestedAt());
        dto.setReturnStatus(order.getReturnStatus());
        dto.setReturnProcessedAt(order.getReturnProcessedAt());
        dto.setRefundAmount(order.getRefundAmount());
        dto.setRefundNotes(order.getRefundNotes());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getGstAmount() { return gstAmount; }
    public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }

    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(BigDecimal deliveryCharge) { this.deliveryCharge = deliveryCharge; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Order.OrderStatus getStatus() { return status; }
    public void setStatus(Order.OrderStatus status) { this.status = status; }

    public Order.PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Order.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTrackingLocation() { return trackingLocation; }
    public void setTrackingLocation(String trackingLocation) { this.trackingLocation = trackingLocation; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPaymentUtr() { return paymentUtr; }
    public void setPaymentUtr(String paymentUtr) { this.paymentUtr = paymentUtr; }

    public String getPaymentScreenshotUrl() { return paymentScreenshotUrl; }
    public void setPaymentScreenshotUrl(String paymentScreenshotUrl) { this.paymentScreenshotUrl = paymentScreenshotUrl; }

    public Boolean getPaymentVerified() { return paymentVerified; }
    public void setPaymentVerified(Boolean paymentVerified) { this.paymentVerified = paymentVerified; }

    public Boolean getReturnRequested() { return returnRequested; }
    public void setReturnRequested(Boolean returnRequested) { this.returnRequested = returnRequested; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public LocalDateTime getReturnRequestedAt() { return returnRequestedAt; }
    public void setReturnRequestedAt(LocalDateTime returnRequestedAt) { this.returnRequestedAt = returnRequestedAt; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public LocalDateTime getReturnProcessedAt() { return returnProcessedAt; }
    public void setReturnProcessedAt(LocalDateTime returnProcessedAt) { this.returnProcessedAt = returnProcessedAt; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundNotes() { return refundNotes; }
    public void setRefundNotes(String refundNotes) { this.refundNotes = refundNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
