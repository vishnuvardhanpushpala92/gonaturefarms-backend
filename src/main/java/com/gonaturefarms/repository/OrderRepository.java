package com.gonaturefarms.repository;

import com.gonaturefarms.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByPhoneOrderByCreatedAtDesc(String phone);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByOrderId(String orderId);

    void deleteByOrderId(String orderId);

    long countByStatus(Order.OrderStatus status);
}
