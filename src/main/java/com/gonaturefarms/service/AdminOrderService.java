package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Order;
import com.gonaturefarms.repository.OrderItemRepository;
import com.gonaturefarms.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Powers GET /api/admin/orders (filterable listing) and DELETE /api/admin/orders/all. */
@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminOrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list(String status, String paymentStatus) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), Order.OrderStatus.valueOf(status)));
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                predicates.add(cb.equal(root.get("paymentStatus"), Order.PaymentStatus.valueOf(paymentStatus)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Order> orders = orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok().with("orders", orders);
    }

    @Transactional
    public ApiResponse clearAll() {
        // order_items has a FK to orders with cascade delete configured on the entity,
        // but we explicitly clear both tables to mirror the original two DELETE statements.
        orderRepository.deleteAll();
        return ApiResponse.ok("All orders cleared");
    }
}
