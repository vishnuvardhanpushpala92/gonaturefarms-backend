package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.AdminOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** GET /api/admin/orders and DELETE /api/admin/orders/all. */
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public ApiResponse list(@RequestParam(required = false) String status,
                             @RequestParam(name = "payment_status", required = false) String paymentStatus) {
        return adminOrderService.list(status, paymentStatus);
    }

    @DeleteMapping("/all")
    public ApiResponse clearAll() {
        return adminOrderService.clearAll();
    }
}
