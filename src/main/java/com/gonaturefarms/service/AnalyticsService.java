package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Order;
import com.gonaturefarms.repository.OrderItemRepository;
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Powers GET /api/admin/analytics — dashboard totals, monthly trend, top products, recent orders. */
@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AnalyticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                             UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse analytics() {
        List<Order> allOrders = orderRepository.findAll();

        long totalOrders = allOrders.size();
        BigDecimal totalRevenue = allOrders.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long delivered = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.Delivered).count();
        long pending = allOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.Pending).count();

        long userCount = userRepository.countByRole(com.gonaturefarms.entity.User.UserRole.customer);
        long productCount = productRepository.count();

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("total_orders", totalOrders);
        totals.put("total_revenue", totalRevenue);
        totals.put("delivered", delivered);
        totals.put("pending", pending);
        totals.put("users", userCount);
        totals.put("products", productCount);

        // Monthly trend for the last 12 months, mirroring DATE_FORMAT(created_at,'%Y-%m') GROUP BY
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        Map<String, long[]> counts = new TreeMap<>();
        Map<String, BigDecimal> revenue = new TreeMap<>();
        for (Order o : allOrders) {
            if (o.getCreatedAt() == null || o.getCreatedAt().isBefore(since)) continue;
            String month = o.getCreatedAt().format(monthFmt);
            counts.merge(month, new long[]{1}, (a, b) -> new long[]{a[0] + b[0]});
            revenue.merge(month, o.getTotal(), BigDecimal::add);
        }
        List<Map<String, Object>> monthly = counts.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", e.getKey());
            m.put("orders", e.getValue()[0]);
            m.put("revenue", revenue.getOrDefault(e.getKey(), BigDecimal.ZERO));
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> topProducts = orderItemRepository.findTopSellingProducts().stream()
                .limit(10)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("product_name", p.getProductName());
                    m.put("sold", p.getSold());
                    m.put("revenue", p.getRevenue());
                    return m;
                }).collect(Collectors.toList());

        List<Map<String, Object>> recentOrders = allOrders.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("order_id", o.getOrderId());
                    m.put("customer_name", o.getCustomerName());
                    m.put("total", o.getTotal());
                    m.put("status", o.getStatus());
                    m.put("created_at", o.getCreatedAt());
                    return m;
                }).collect(Collectors.toList());

        return ApiResponse.ok()
                .with("totals", totals)
                .with("monthly", monthly)
                .with("topProds", topProducts)
                .with("recentOrders", recentOrders);
    }
}
