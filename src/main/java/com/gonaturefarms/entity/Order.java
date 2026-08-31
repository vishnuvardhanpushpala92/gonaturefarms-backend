package com.gonaturefarms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @Column(length = 100)
    private String area = "";

    @Column(nullable = false, length = 80)
    private String city;

    @Builder.Default
    @Column(length = 80)
    private String state = "";

    @Column(nullable = false, length = 10)
    private String pincode;

    @Builder.Default
    @Column(name = "payment_method", length = 30)
    private String paymentMethod = "UPI";

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "gst_amount", precision = 10, scale = 2)
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "delivery_charge", precision = 10, scale = 2)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.Pending;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.Pending;

    @Builder.Default
    @Column(name = "tracking_location", length = 255)
    private String trackingLocation = "";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "payment_utr", length = 50)
    private String paymentUtr = "";

    @Builder.Default
    @Column(name = "payment_screenshot_url", length = 500)
    private String paymentScreenshotUrl = "";

    @Column(name = "payment_verified")
    @Builder.Default
    private Boolean paymentVerified = false;

    @Builder.Default
    @Column(name = "return_requested", nullable = false)
    private Boolean returnRequested = false;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "return_requested_at")
    private LocalDateTime returnRequestedAt;

    @Builder.Default
    @Column(name = "return_status", length = 30)
    private String returnStatus = "None";

    @Column(name = "return_processed_at")
    private LocalDateTime returnProcessedAt;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_notes", length = 500)
    private String refundNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    // Fixed methods – explicit type hints to avoid inference issues
    @Transient
    public String getItemsSummary() {
        if (items == null || items.isEmpty()) return "";
        return items.stream()
                .map(i -> i.getProductName() + "\u00D7" + i.getQuantity())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    @Transient
    public String getItemsList() {
        if (items == null || items.isEmpty()) return "";
        return items.stream()
                .map(i -> i.getProductName() + " x" + i.getQuantity() + "|" +
                        (i.getProductImage() == null ? "" : i.getProductImage()))
                .collect(java.util.stream.Collectors.joining("||"));
    }

    public enum OrderStatus {
        Pending, Placed, Confirmed, Processing, Packed, Shipped, OutForDelivery, Delivered, Cancelled, PaymentVerificationPending
    }

    public enum PaymentStatus {
        Pending, Paid, Failed, Refunded
    }

    // Manual getters as failsafe for Lombok processing issues
    public Long getId() { return id; }

    public String getOrderId() { return orderId; }

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

    public BigDecimal getSubtotal() { return subtotal; }

    public BigDecimal getGstAmount() { return gstAmount; }

    public BigDecimal getDeliveryCharge() { return deliveryCharge; }

    public BigDecimal getDiscount() { return discount; }

    public BigDecimal getTotal() { return total; }

    public OrderStatus getStatus() { return status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }

    public String getTrackingLocation() { return trackingLocation; }

    public String getNotes() { return notes; }

    public String getPaymentScreenshotUrl() { return paymentScreenshotUrl; }

    public Boolean getPaymentVerified() { return paymentVerified; }

    public Boolean getReturnRequested() { return returnRequested; }

    public String getReturnReason() { return returnReason; }

    public LocalDateTime getReturnRequestedAt() { return returnRequestedAt; }

    public String getReturnStatus() { return returnStatus; }

    public LocalDateTime getReturnProcessedAt() { return returnProcessedAt; }

    public BigDecimal getRefundAmount() { return refundAmount; }

    public String getRefundNotes() { return refundNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }

    // Static builder method as failsafe for Lombok @Builder
    public static OrderBuilder builder() {
        return new OrderBuilder();
    }

    public static class OrderBuilder {
        private String orderId;
        private Long userId;
        private String customerName;
        private String phone;
        private String email;
        private String address;
        private String area = "";
        private String city;
        private String state = "";
        private String pincode;
        private String paymentMethod = "UPI";
        private String paymentUtr = "";
        private String paymentScreenshotUrl = "";
        private BigDecimal subtotal;
        private BigDecimal gstAmount;
        private BigDecimal deliveryCharge;
        private BigDecimal discount;
        private BigDecimal total;
        private OrderStatus status = OrderStatus.Pending;   // FIXED: changed from Placed to Pending
        private PaymentStatus paymentStatus = PaymentStatus.Pending;
        private String trackingLocation = "";
        private String notes;
        private Boolean paymentVerified = false;
        private Boolean returnRequested = false;
        private String returnReason;
        private LocalDateTime returnRequestedAt;
        private String returnStatus = "None";
        private LocalDateTime returnProcessedAt;
        private BigDecimal refundAmount;
        private String refundNotes;
        private LocalDateTime createdAt = LocalDateTime.now();
        private List<OrderItem> items = new java.util.ArrayList<>();

        public OrderBuilder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public OrderBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public OrderBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public OrderBuilder email(String email) {
            this.email = email;
            return this;
        }

        public OrderBuilder address(String address) {
            this.address = address;
            return this;
        }

        public OrderBuilder area(String area) {
            this.area = area;
            return this;
        }

        public OrderBuilder city(String city) {
            this.city = city;
            return this;
        }

        public OrderBuilder state(String state) {
            this.state = state;
            return this;
        }

        public OrderBuilder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public OrderBuilder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public OrderBuilder paymentUtr(String paymentUtr) {
            this.paymentUtr = paymentUtr;
            return this;
        }

        public OrderBuilder paymentScreenshotUrl(String paymentScreenshotUrl) {
            this.paymentScreenshotUrl = paymentScreenshotUrl;
            return this;
        }

        public OrderBuilder subtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public OrderBuilder gstAmount(BigDecimal gstAmount) {
            this.gstAmount = gstAmount;
            return this;
        }

        public OrderBuilder deliveryCharge(BigDecimal deliveryCharge) {
            this.deliveryCharge = deliveryCharge;
            return this;
        }

        public OrderBuilder discount(BigDecimal discount) {
            this.discount = discount;
            return this;
        }

        public OrderBuilder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public OrderBuilder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public OrderBuilder paymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }

        public OrderBuilder trackingLocation(String trackingLocation) {
            this.trackingLocation = trackingLocation;
            return this;
        }

        public OrderBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public OrderBuilder paymentVerified(Boolean paymentVerified) {
            this.paymentVerified = paymentVerified;
            return this;
        }

        public OrderBuilder returnRequested(Boolean returnRequested) {
            this.returnRequested = returnRequested;
            return this;
        }

        public OrderBuilder returnReason(String returnReason) {
            this.returnReason = returnReason;
            return this;
        }

        public OrderBuilder returnRequestedAt(LocalDateTime returnRequestedAt) {
            this.returnRequestedAt = returnRequestedAt;
            return this;
        }

        public OrderBuilder returnStatus(String returnStatus) {
            this.returnStatus = returnStatus;
            return this;
        }

        public OrderBuilder returnProcessedAt(LocalDateTime returnProcessedAt) {
            this.returnProcessedAt = returnProcessedAt;
            return this;
        }

        public OrderBuilder refundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
            return this;
        }

        public OrderBuilder refundNotes(String refundNotes) {
            this.refundNotes = refundNotes;
            return this;
        }

        public OrderBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrderBuilder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Order build() {
            Order order = new Order();
            order.orderId = this.orderId;
            order.userId = this.userId;
            order.customerName = this.customerName;
            order.phone = this.phone;
            order.email = this.email;
            order.address = this.address;
            order.area = this.area;
            order.city = this.city;
            order.state = this.state;
            order.pincode = this.pincode;
            order.paymentMethod = this.paymentMethod;
            order.paymentUtr = this.paymentUtr;
            order.paymentScreenshotUrl = this.paymentScreenshotUrl;
            order.subtotal = this.subtotal;
            order.gstAmount = this.gstAmount;
            order.deliveryCharge = this.deliveryCharge;
            order.discount = this.discount;
            order.total = this.total;
            order.status = this.status;
            order.paymentStatus = this.paymentStatus;
            order.trackingLocation = this.trackingLocation;
            order.notes = this.notes;
            order.paymentVerified = this.paymentVerified;
            order.returnRequested = this.returnRequested;
            order.returnReason = this.returnReason;
            order.returnRequestedAt = this.returnRequestedAt;
            order.returnStatus = this.returnStatus;
            order.returnProcessedAt = this.returnProcessedAt;
            order.refundAmount = this.refundAmount;
            order.refundNotes = this.refundNotes;
            order.createdAt = this.createdAt;
            order.items = this.items;
            return order;
        }
    }
}