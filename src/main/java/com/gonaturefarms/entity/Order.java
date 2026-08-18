package com.gonaturefarms.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Maps to the "orders" table. */
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
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.Pending;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.Pending;

    @Builder.Default
    @Column(name = "tracking_location", length = 255)
    private String trackingLocation = "";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "payment_utr", length = 50)
    private String paymentUtr;

    @Column(name = "payment_screenshot_url", length = 500)
    private String paymentScreenshotUrl;

    @Column(name = "payment_verified")
    @Builder.Default
    private Boolean paymentVerified = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Mirrors the original SQL's GROUP_CONCAT(product_name × quantity) alias
     * "items_summary", used directly by the frontend on the order-tracking page.
     */
    @Transient
    public String getItemsSummary() {
        if (items == null || items.isEmpty()) return "";
        return items.stream()
                .map(i -> i.getProductName() + "\u00D7" + i.getQuantity())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /**
     * Mirrors the original SQL's GROUP_CONCAT('name xQTY|image' SEPARATOR '||') alias
     * "items_list", used directly by the admin orders panel.
     */
    @Transient
    public String getItemsList() {
        if (items == null || items.isEmpty()) return "";
        return items.stream()
                .map(i -> i.getProductName() + " x" + i.getQuantity() + "|" +
                        (i.getProductImage() == null ? "" : i.getProductImage()))
                .collect(java.util.stream.Collectors.joining("||"));
    }

    public enum OrderStatus {
        Pending,Placed, Confirmed, Processing, Packed, Shipped, OutForDelivery, Delivered, Cancelled, PaymentVerificationPending
    }

    public enum PaymentStatus {
        Pending, Paid, Failed, Refunded
    }
}
