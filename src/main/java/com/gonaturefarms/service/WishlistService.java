package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Wishlist;
import com.gonaturefarms.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Business logic for the customer wishlist. Mirrors routes/wishlist.js. */
@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistService(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse getWishlist(Long userId) {
        List<WishlistRepository.WishlistItemProjection> items = wishlistRepository.findWishlistWithProductDetails(userId);
        return ApiResponse.ok().with("wishlist", items);
    }

    @Transactional(readOnly = true)
    public ApiResponse getWishlistIds(Long userId) {
        List<Long> ids = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(Wishlist::getProductId).collect(Collectors.toList());
        return ApiResponse.ok().with("ids", ids);
    }

    @Transactional
    public ApiResponse add(Long userId, Long productId) {
        // INSERT IGNORE semantics: silently do nothing if already present
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            try {
                wishlistRepository.save(Wishlist.builder().userId(userId).productId(productId).build());
            } catch (org.springframework.dao.DataIntegrityViolationException ignored) {
                // Already added concurrently — treat as success, same as MySQL's INSERT IGNORE
            }
        }
        return ApiResponse.ok("Added to wishlist");
    }

    @Transactional
    public ApiResponse remove(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        return ApiResponse.ok("Removed from wishlist");
    }
}
