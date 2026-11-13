package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByPhoneOrderByCreatedAtDesc(String phone);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByOrderId(String orderId);

    void deleteByOrderId(String orderId);

    long countByStatus(Order.OrderStatus status);

    /**
     * Find distinct customer IDs who have purchased a specific product
     * Uses a direct SQL query to avoid lazy loading issues
     */
    @Query("SELECT DISTINCT o.userId FROM Order o JOIN o.items i WHERE i.productId = :productId")
    Set<Long> findCustomerIdsByProductId(@Param("productId") Long productId);
}
