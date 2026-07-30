package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.coupon.CouponRequest;
import com.gonaturefarms.dto.coupon.CouponValidateRequest;
import com.gonaturefarms.service.CouponService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for discount coupons. Mirrors routes/coupons.js. */
@RestController
@RequestMapping("/api/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    @PreAuthorize("permitAll()")
    public ApiResponse validate(@RequestBody CouponValidateRequest request) {
        return couponService.validate(request);
    }

    @GetMapping
    public ApiResponse list() {
        return couponService.listAll();
    }

    @PostMapping
    public ApiResponse create(@RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse toggle(@PathVariable Long id) {
        return couponService.toggle(id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        return couponService.delete(id);
    }
}
