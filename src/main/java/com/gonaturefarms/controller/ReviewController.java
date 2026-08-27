package com.gonaturefarms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.review.AdminReviewRequest;
import com.gonaturefarms.dto.review.FeatureToggleRequest;
import com.gonaturefarms.dto.review.ReviewRequest;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/home/featured")
    public ApiResponse featured() {
        return reviewService.getFeaturedReviews();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminAll() {
        return reviewService.adminAll();
    }

    // ✅ FALLBACK: To prevent 404 errors from old frontend builds
    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse fallbackAdminReviews() {
        return reviewService.adminAll();
    }

    @PostMapping("/admin/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminAdd(@PathVariable Long productId, @RequestBody AdminReviewRequest request) {
        return reviewService.adminAdd(SecurityUtils.requireCurrentUser().id(), productId, request);
    }

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminCreate(@RequestBody AdminReviewRequest request) {
        return reviewService.adminCreate(SecurityUtils.requireCurrentUser().id(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody AdminReviewRequest request) {
        return reviewService.adminUpdate(id, request);
    }

    @GetMapping("/{productId}")
    public ApiResponse forProduct(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    @PostMapping("/{productId}")
    public ApiResponse submit(@PathVariable Long productId, @RequestBody ReviewRequest request) {
        return reviewService.submitReview(SecurityUtils.requireCurrentUser().id(), productId, request);
    }

    @PutMapping("/{id}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse feature(@PathVariable Long id, @RequestBody FeatureToggleRequest request) {
        return reviewService.feature(id, Boolean.TRUE.equals(request.getFeatured()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse approve(@PathVariable Long id) {
        return reviewService.approve(id);
    }

    @PutMapping("/{id}/unapprove")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse unapprove(@PathVariable Long id) {
        return reviewService.unapprove(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return reviewService.delete(id);
    }
}