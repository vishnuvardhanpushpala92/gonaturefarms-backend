package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, Review.ReviewStatus status);

    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'approved'")
    Double findAverageRating(@Param("productId") Long productId);

    long countByProductIdAndStatus(Long productId, Review.ReviewStatus status);

    List<Review> findByStatusAndFeaturedOrderByCreatedAtDesc(Review.ReviewStatus status, boolean featured, Pageable pageable);

    /**
     * All reviews ordered by creation date (newest first). The original SQL ordered
     * pending reviews first via "ORDER BY (status='pending') DESC, created_at DESC";
     * that pending-first re-ordering is applied in the service layer to keep this
     * query portable and avoid a fragile enum literal inside JPQL.
     */
    List<Review> findAllByOrderByCreatedAtDesc();
}
