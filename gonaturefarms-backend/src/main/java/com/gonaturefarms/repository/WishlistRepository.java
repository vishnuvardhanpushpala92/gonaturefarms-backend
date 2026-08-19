package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    /** Mirrors: SELECT w.product_id, p.* FROM wishlist w JOIN products p ON p.id = w.product_id WHERE w.user_id = ? */
    @Query("SELECT w.productId as productId, p.name as name, p.price as price, p.mrp as mrp, " +
           "p.gst as gst, p.imgUrl as imgUrl, p.cat as cat, p.status as status " +
           "FROM Wishlist w JOIN Product p ON p.id = w.productId " +
           "WHERE w.userId = :userId ORDER BY w.createdAt DESC")
    List<WishlistItemProjection> findWishlistWithProductDetails(@Param("userId") Long userId);

    interface WishlistItemProjection {
        Long getProductId();
        String getName();
        java.math.BigDecimal getPrice();
        java.math.BigDecimal getMrp();
        java.math.BigDecimal getGst();
        String getImgUrl();
        String getCat();
        com.gonaturefarms.entity.Product.ProductStatus getStatus();
    }
}
