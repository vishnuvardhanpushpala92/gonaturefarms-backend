package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.UpiQrService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Admin-only management of the UPI QR image shown at checkout. */
@RestController
@RequestMapping("/api/admin/payment-settings")
public class PaymentSettingsController {

    private final UpiQrService upiQrService;

    public PaymentSettingsController(UpiQrService upiQrService) {
        this.upiQrService = upiQrService;
    }

    @GetMapping("/upi-qr")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse getUpiQr() {
        return upiQrService.current();
    }

    @PostMapping("/upi-qr")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse uploadUpiQr(@RequestParam("file") MultipartFile file) {
        return upiQrService.upload(file);
    }

    @DeleteMapping("/upi-qr")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse removeUpiQr() {
        return upiQrService.remove();
    }
}
