package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.order.OrderRequest;
import com.gonaturefarms.dto.order.OrderStatusUpdateRequest;
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
    public ApiResponse place(@Valid @RequestBody OrderRequest request) {
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
}
