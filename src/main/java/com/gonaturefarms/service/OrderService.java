package com.gonaturefarms.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.order.OrderItemRequest;
import com.gonaturefarms.dto.order.OrderRequest;
import com.gonaturefarms.dto.order.OrderStatusUpdateRequest;
import com.gonaturefarms.entity.Order;
import com.gonaturefarms.entity.OrderItem;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.exception.ResourceNotFoundException;
import com.gonaturefarms.repository.CouponRepository;
import com.gonaturefarms.repository.DeliveryZoneRepository;
import com.gonaturefarms.repository.OrderRepository; // <--- Added this missing import
import com.gonaturefarms.util.OrderIdGenerator;

/** Business logic for placing, looking up, and (admin) managing orders. Mirrors routes/orders.js. */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final CouponRepository couponRepository;

    public OrderService(OrderRepository orderRepository,
                         DeliveryZoneRepository deliveryZoneRepository,
                         CouponRepository couponRepository) {
        this.orderRepository = orderRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public ApiResponse placeOrder(OrderRequest req) {
        if (isBlank(req.getCustomerName()) || isBlank(req.getPhone()) || isBlank(req.getAddress())
                || isBlank(req.getCity()) || isBlank(req.getPincode())
                || req.getItems() == null || req.getItems().isEmpty()) {
            throw new ApiException("Missing required order fields");
        }

        // Verify pincode is serviceable (only enforced once at least one zone is configured)
        long zoneCount = deliveryZoneRepository.count();
        boolean zoneKnown = deliveryZoneRepository.findByPincode(req.getPincode().trim()).isPresent();
        if (zoneCount > 0 && !zoneKnown) {
            throw new ApiException("We don't deliver to pincode " + req.getPincode() + " yet.");
        }

        // 🔁 LOOP: Retry generating a new ID until it successfully saves without a conflict
        while (true) {
            try {
                // Generate a unique order ID inside the loop
                String newOrderId = OrderIdGenerator.generate();

                Order order = Order.builder()
                        .orderId(newOrderId) // Use the newly generated ID
                        .userId(req.getUserId())
                        .customerName(req.getCustomerName())
                        .phone(req.getPhone())
                        .email(isBlank(req.getEmail()) ? null : req.getEmail())
                        .address(req.getAddress())
                        .area(req.getArea() == null ? "" : req.getArea())
                        .city(req.getCity())
                        .state(req.getState() == null ? "" : req.getState())
                        .pincode(req.getPincode())
                        .paymentMethod(isBlank(req.getPaymentMethod()) ? "UPI" : req.getPaymentMethod())
                        .paymentUtr(req.getPaymentUtr())
                        .subtotal(nz(req.getSubtotal()))
                        .gstAmount(nz(req.getGstAmount()))
                        .deliveryCharge(nz(req.getDeliveryCharge()))
                        .discount(nz(req.getDiscount()))
                        .total(req.getTotal())
                        .status(req.getPaymentMethod() != null && req.getPaymentMethod().equalsIgnoreCase("UPI") 
                                ? Order.OrderStatus.PaymentVerificationPending 
                                : Order.OrderStatus.Placed)
                        .paymentStatus(Order.PaymentStatus.Pending)
                        .build();

                for (OrderItemRequest item : req.getItems()) {
                    OrderItem orderItem = OrderItem.builder()
                            .productId(item.getId())
                            .productName(item.getName())
                            .productImage(item.getImg() == null ? "" : item.getImg())
                            .price(item.getPrice())
                            .gst(nz(item.getGst()))
                            .quantity(item.getQty())
                            .total(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())))
                            .order(order)
                            .build();
                    order.getItems().add(orderItem);
                }

                // Save the order
                order = orderRepository.save(order);

                // Apply coupon usage
                if (!isBlank(req.getCouponCode())) {
                    couponRepository.findByCode(req.getCouponCode().toUpperCase())
                            .ifPresent(c -> {
                                c.setUsedCount(c.getUsedCount() + 1);
                                couponRepository.save(c);
                            });
                }

                // If we reach here, saving succeeded! Return the success response.
                return ApiResponse.ok("Order placed successfully!")
                        .with("order_id", order.getOrderId())
                        .with("id", order.getId());

            } catch (DataIntegrityViolationException e) {
                // If a duplicate ID was generated, catch the error and silently loop again
                System.err.println("⚠️ Duplicate order_id generated, retrying with new ID...");
                // The loop continues and generates a brand new ID.
            }
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse lookupByPhone(String phone) {
        if (isBlank(phone)) {
            throw new ApiException("Phone required");
        }
        List<Order> orders = orderRepository.findByPhoneOrderByCreatedAtDesc(phone.trim());
        return ApiResponse.ok().with("orders", orders);
    }

    @Transactional(readOnly = true)
    public ApiResponse myOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ApiResponse.ok().with("orders", orders);
    }

    @Transactional(readOnly = true)
    public ApiResponse getOrderDetail(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return ApiResponse.ok().with("order", order);
    }

    @Transactional
    public ApiResponse updateStatus(String orderId, OrderStatusUpdateRequest req) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!isBlank(req.getStatus())) {
            order.setStatus(Order.OrderStatus.valueOf(req.getStatus()));
        }
        if (!isBlank(req.getPaymentStatus())) {
            order.setPaymentStatus(Order.PaymentStatus.valueOf(req.getPaymentStatus()));
        }
        if (req.getTrackingLocation() != null) {
            order.setTrackingLocation(req.getTrackingLocation());
        }
        orderRepository.save(order);
        return ApiResponse.ok("Order updated");
    }

    @Transactional
    public ApiResponse deleteOrder(String orderId) {
        orderRepository.findByOrderId(orderId).ifPresent(orderRepository::delete);
        return ApiResponse.ok("Order deleted");
    }

    @Transactional
    public ApiResponse verifyPayment(String orderId, boolean approved) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (approved) {
            order.setPaymentVerified(true);
            order.setPaymentStatus(Order.PaymentStatus.Paid);
            order.setStatus(Order.OrderStatus.Confirmed);
        } else {
            order.setPaymentVerified(false);
            order.setPaymentStatus(Order.PaymentStatus.Failed);
            order.setStatus(Order.OrderStatus.Cancelled);
        }

        orderRepository.save(order);
        return ApiResponse.ok(approved ? "Payment verified and order confirmed" : "Payment rejected and order cancelled");
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}