package com.gonaturefarms.repository;

import com.gonaturefarms.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductVariant pv WHERE pv.product.id = :productId")
    void deleteByProductId(Long productId);
}