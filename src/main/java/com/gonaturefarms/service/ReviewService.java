package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.review.AdminReviewRequest;
import com.gonaturefarms.dto.review.ReviewRequest;
import com.gonaturefarms.entity.Review;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.repository.ReviewRepository;
import com.gonaturefarms.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Business logic for product reviews and their admin moderation workflow. Mirrors routes/reviews.js. */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
                          ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(
                productId, Review.ReviewStatus.approved);
        List<Map<String, Object>> withNames = reviews.stream().map(this::withDisplayName).collect(Collectors.toList());

        Double avg = reviewRepository.findAverageRating(productId);
        long count = reviewRepository.countByProductIdAndStatus(productId, Review.ReviewStatus.approved);

        return ApiResponse.ok()
                .with("reviews", withNames)
                .with("avg_rating", avg == null ? 0 : avg)
                .with("count", count);
    }

    @Transactional(readOnly = true)
    public ApiResponse getFeaturedReviews() {
        List<Review> reviews = reviewRepository.findByStatusAndFeaturedOrderByCreatedAtDesc(
                Review.ReviewStatus.approved, true, PageRequest.of(0, 6));
        List<Map<String, Object>> withDetails = reviews.stream().map(r -> {
            Map<String, Object> map = withDisplayName(r);
            productRepository.findById(r.getProductId())
                    .ifPresent(p -> map.put("product_name", p.getName()));
            return map;
        }).collect(Collectors.toList());
        return ApiResponse.ok().with("reviews", withDetails);
    }

    @Transactional
    public ApiResponse submitReview(Long userId, Long productId, ReviewRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new ApiException("Rating must be 1-5");
        }
        Review review = reviewRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> Review.builder().userId(userId).productId(productId).build());
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        review.setStatus(Review.ReviewStatus.pending);
        review.setCreatedAt(java.time.LocalDateTime.now());
        reviewRepository.save(review);
        return ApiResponse.ok("Thanks! Your review has been submitted and is awaiting approval.");
    }

    @Transactional(readOnly = true)
    public ApiResponse adminAll() {
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        // Pending-first ordering, mirroring "ORDER BY (status='pending') DESC, created_at DESC"
        List<Map<String, Object>> ordered = reviews.stream()
                .sorted(Comparator.comparing((Review r) -> r.getStatus() != Review.ReviewStatus.pending))
                .map(r -> {
                    Map<String, Object> map = withDisplayName(r);
                    productRepository.findById(r.getProductId()).ifPresent(p -> map.put("product_name", p.getName()));
                    return map;
                })
                .collect(Collectors.toList());
        return ApiResponse.ok().with("reviews", ordered);
    }

    @Transactional
    public ApiResponse approve(Long id) {
        Review review = getOrThrow(id);
        review.setStatus(Review.ReviewStatus.approved);
        reviewRepository.save(review);
        return ApiResponse.ok("Review approved and published");
    }

    @Transactional
    public ApiResponse unapprove(Long id) {
        Review review = getOrThrow(id);
        review.setStatus(Review.ReviewStatus.pending);
        review.setFeatured(false);
        reviewRepository.save(review);
        return ApiResponse.ok("Review hidden");
    }

    @Transactional
    public ApiResponse feature(Long id, boolean featured) {
        Review review = getOrThrow(id);
        review.setFeatured(featured);
        reviewRepository.save(review);
        return ApiResponse.ok(featured ? "Added to homepage" : "Removed from homepage");
    }

    @Transactional
    public ApiResponse adminAdd(Long adminUserId, Long productId, AdminReviewRequest req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new ApiException("Rating must be 1-5");
        }
        if (req.getUserName() == null || req.getUserName().isBlank()) {
            throw new ApiException("Customer name is required");
        }
        Review review = Review.builder()
                .userId(adminUserId)
                .productId(productId)
                .rating(req.getRating())
                .comment(req.getComment())
                .customerName(req.getUserName())
                .status(Review.ReviewStatus.approved)
                .build();
        reviewRepository.save(review);
        return ApiResponse.ok("Review added by admin");
    }

    @Transactional
    public ApiResponse adminCreate(Long adminUserId, AdminReviewRequest req) {
        if (req.getProductId() == null) {
            throw new ApiException("Product ID is required");
        }
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new ApiException("Rating must be 1-5");
        }
        if (req.getUserName() == null || req.getUserName().isBlank()) {
            throw new ApiException("User name is required");
        }
        Review review = Review.builder()
                .userId(adminUserId)
                .productId(req.getProductId())
                .rating(req.getRating())
                .comment(req.getComment())
                .customerName(req.getUserName())
                .status(Review.ReviewStatus.approved)
                .featured(Boolean.TRUE.equals(req.getFeatured()))
                .build();
        reviewRepository.save(review);
        return ApiResponse.ok("Review created by admin");
    }

    @Transactional
    public ApiResponse adminUpdate(Long id, AdminReviewRequest req) {
        Review review = getOrThrow(id);
        if (req.getRating() != null) {
            if (req.getRating() < 1 || req.getRating() > 5) {
                throw new ApiException("Rating must be 1-5");
            }
            review.setRating(req.getRating());
        }
        if (req.getUserName() != null && !req.getUserName().isBlank()) {
            review.setCustomerName(req.getUserName());
        }
        if (req.getComment() != null) {
            review.setComment(req.getComment());
        }
        if (req.getFeatured() != null) {
            review.setFeatured(req.getFeatured());
        }
        reviewRepository.save(review);
        return ApiResponse.ok("Review updated");
    }

    @Transactional
    public ApiResponse delete(Long id) {
        reviewRepository.deleteById(id);
        return ApiResponse.ok("Review deleted");
    }

    private Review getOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Review not found"));
    }

    /** Mirrors: COALESCE(NULLIF(r.customer_name,''), u.name) AS user_name */
    private Map<String, Object> withDisplayName(Review r) {
        String displayName = (r.getCustomerName() != null && !r.getCustomerName().isBlank())
                ? r.getCustomerName()
                : userRepository.findById(r.getUserId()).map(com.gonaturefarms.entity.User::getName).orElse("Guest");
        java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("user_id", r.getUserId());
        map.put("product_id", r.getProductId());
        map.put("rating", r.getRating());
        map.put("comment", r.getComment());
        map.put("customer_name", r.getCustomerName());
        map.put("status", r.getStatus());
        map.put("featured", r.getFeatured());
        map.put("created_at", r.getCreatedAt());
        map.put("user_name", displayName);
        return map;
    }
}
