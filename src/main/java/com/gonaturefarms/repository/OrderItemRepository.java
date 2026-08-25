package com.gonaturefarms.repository;

import com.gonaturefarms.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    /** Aggregated best-sellers, mirrors: GROUP BY product_name ORDER BY sold DESC LIMIT 10 */
    @Query("SELECT oi.productName as productName, SUM(oi.quantity) as sold, SUM(oi.total) as revenue " +
           "FROM OrderItem oi GROUP BY oi.productName ORDER BY SUM(oi.quantity) DESC")
    List<TopProductProjection> findTopSellingProducts();

    interface TopProductProjection {
        String getProductName();
        Long getSold();
        java.math.BigDecimal getRevenue();
    }
}
