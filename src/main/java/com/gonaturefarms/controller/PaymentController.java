package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.order.OrderRequest;
import com.gonaturefarms.security.CurrentUser;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.UpiPaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Free direct-UPI payment endpoints.
 *
 * There is no payment gateway: the customer pays the merchant's own UPI ID
 * directly. Orders stay PENDING until the customer confirms, then move to
 * VERIFICATION_PENDING until an admin verifies the money in their UPI/bank app.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final UpiPaymentService upiPaymentService;

    public PaymentController(UpiPaymentService upiPaymentService) {
        this.upiPaymentService = upiPaymentService;
    }

    /** Merchant UPI details for the payment screen. */
    @GetMapping("/config")
    public ApiResponse config() {
        return upiPaymentService.upiConfig();
    }

    /**
     * Creates a pending order with server-verified totals and returns the
     * order-specific UPI link + QR code.
     */
    @PostMapping("/create")
    public ApiResponse create(@RequestBody OrderRequest request) {
        return upiPaymentService.createCheckout(request);
    }

    /**
     * Customer clicked "I HAVE COMPLETED PAYMENT".
     * Moves the order to VERIFICATION_PENDING — never marks it paid.
     */
    @PostMapping("/{orderId}/confirm")
    public ApiResponse confirm(@PathVariable String orderId) {
        CurrentUser user = SecurityUtils.getCurrentUser().orElse(null);
        return upiPaymentService.confirmPaymentCompleted(orderId, user);
    }

    /** Poll current payment status (e.g. after returning from a UPI app). */
    @GetMapping("/{orderId}/status")
    public ApiResponse status(@PathVariable String orderId) {
        return upiPaymentService.paymentStatus(orderId);
    }
}
