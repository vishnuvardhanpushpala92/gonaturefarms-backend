package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.order.OrderRequest;
import com.gonaturefarms.dto.order.OrderStatusUpdateRequest;
import com.gonaturefarms.dto.order.RefundRequest;
import com.gonaturefarms.dto.order.ReturnRequest;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for orders. Mirrors routes/orders.js. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse place(@RequestBody OrderRequest request) {
        System.out.println("=== ORDER REQUEST RECEIVED ===");
        System.out.println("Customer Name: " + request.getCustomerName());
        System.out.println("Phone: " + request.getPhone());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Address: " + request.getAddress());
        System.out.println("Area: " + request.getArea());
        System.out.println("City: " + request.getCity());
        System.out.println("State: " + request.getState());
        System.out.println("Pincode: " + request.getPincode());
        System.out.println("Payment Method: " + request.getPaymentMethod());
        System.out.println("Payment UTR: " + request.getPaymentUtr());
        System.out.println("Items count: " + (request.getItems() != null ? request.getItems().size() : 0));
        System.out.println("User ID: " + request.getUserId());
        return orderService.placeOrder(request);
    }

    @GetMapping("/lookup")
    public ApiResponse lookup(@RequestParam String phone) {
        return orderService.lookupByPhone(phone);
    }

    @GetMapping("/my")
    public ApiResponse my() {
        return orderService.myOrders(SecurityUtils.requireCurrentUser().id());
    }

    @GetMapping("/{orderId}")
    public ApiResponse detail(@PathVariable String orderId) {
        return orderService.getOrderDetail(orderId);
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse updateStatus(@PathVariable String orderId, @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(orderId, request);
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable String orderId) {
        return orderService.deleteOrder(orderId);
    }

    @PutMapping("/{orderId}/verify-payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse verifyPayment(@PathVariable String orderId, @RequestParam boolean approved) {
        return orderService.verifyPayment(orderId, approved);
    }

    @PostMapping("/{orderId}/return")
    public ApiResponse requestReturn(@PathVariable String orderId, @RequestBody ReturnRequest request) {
        try {
            // Try to get current user if authenticated
            var currentUserOpt = SecurityUtils.getCurrentUser();
            Long userId = currentUserOpt != null && currentUserOpt.isPresent() ? currentUserOpt.get().id() : null;
            return orderService.requestReturn(orderId, request, userId);
        } catch (Exception e) {
            // If authentication fails, proceed without user ID for phone-based returns
            return orderService.requestReturn(orderId, request, null);
        }
    }

    @PutMapping("/{orderId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse processRefund(@PathVariable String orderId, @RequestBody RefundRequest request) {
        return orderService.processRefund(orderId, request);
    }
}