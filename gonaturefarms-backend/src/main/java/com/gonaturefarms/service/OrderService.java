package com.gonaturefarms.service;

import java.math.BigDecimal;
import java.util.List;

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
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.util.OrderIdGenerator;

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
        // Validation
        if (isBlank(req.getCustomerName()) || isBlank(req.getPhone()) || isBlank(req.getAddress())
                || isBlank(req.getCity()) || isBlank(req.getPincode())
                || req.getItems() == null || req.getItems().isEmpty()) {
            throw new ApiException("Missing required order fields");
        }

        long zoneCount = deliveryZoneRepository.count();
        boolean zoneKnown = deliveryZoneRepository.findByPincode(req.getPincode().trim()).isPresent();
        if (zoneCount > 0 && !zoneKnown) {
            throw new ApiException("We don't deliver to pincode " + req.getPincode() + " yet.");
        }

        String newOrderId = OrderIdGenerator.generate();

        Order order = new Order();
        order.setOrderId(newOrderId);
        order.setUserId(req.getUserId());
        order.setCustomerName(req.getCustomerName());
        order.setPhone(req.getPhone());
        order.setEmail(isBlank(req.getEmail()) ? null : req.getEmail());
        order.setAddress(req.getAddress());
        order.setArea(req.getArea() == null ? "" : req.getArea());
        order.setCity(req.getCity());
        order.setState(req.getState() == null ? "" : req.getState());
        order.setPincode(req.getPincode());
        order.setPaymentMethod(isBlank(req.getPaymentMethod()) ? "UPI" : req.getPaymentMethod());
        order.setPaymentUtr(req.getPaymentUtr() == null ? "" : req.getPaymentUtr());
        order.setPaymentScreenshotUrl(req.getPaymentScreenshotUrl() == null ? "" : req.getPaymentScreenshotUrl());
        order.setSubtotal(nz(req.getSubtotal()));
        order.setGstAmount(nz(req.getGstAmount()));
        order.setDeliveryCharge(nz(req.getDeliveryCharge()));
        order.setDiscount(nz(req.getDiscount()));
        order.setTotal(req.getTotal());
        
        // 🔥 FIX: Use "Placed" (6 chars) so it fits ANY column length
        order.setStatus(Order.OrderStatus.Placed);
        order.setPaymentStatus(Order.PaymentStatus.Pending);

        for (OrderItemRequest item : req.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getId());
            orderItem.setProductName(item.getName());
            orderItem.setProductImage(item.getImg() == null ? "" : item.getImg());
            orderItem.setPrice(item.getPrice());
            orderItem.setGst(nz(item.getGst()));
            orderItem.setQuantity(item.getQty());
            orderItem.setTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
        }

        order = orderRepository.save(order);

        if (!isBlank(req.getCouponCode())) {
            couponRepository.findByCode(req.getCouponCode().toUpperCase())
                    .ifPresent(c -> {
                        c.setUsedCount(c.getUsedCount() + 1);
                        couponRepository.save(c);
                    });
        }

        return ApiResponse.ok("Order placed successfully!")
                .with("order_id", order.getOrderId())
                .with("id", order.getId());
    }

    // ... (rest of the methods unchanged: lookupByPhone, myOrders, getOrderDetail, updateStatus, deleteOrder, verifyPayment, nz, isBlank)
}