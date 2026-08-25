package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.WishlistService;
import org.springframework.web.bind.annotation.*;

/** REST controller for the customer wishlist. Mirrors routes/wishlist.js (all endpoints require auth). */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ApiResponse list() {
        return wishlistService.getWishlist(SecurityUtils.requireCurrentUser().id());
    }

    @GetMapping("/ids")
    public ApiResponse ids() {
        return wishlistService.getWishlistIds(SecurityUtils.requireCurrentUser().id());
    }

    @PostMapping("/{productId}")
    public ApiResponse add(@PathVariable Long productId) {
        return wishlistService.add(SecurityUtils.requireCurrentUser().id(), productId);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse remove(@PathVariable Long productId) {
        return wishlistService.remove(SecurityUtils.requireCurrentUser().id(), productId);
    }
}
