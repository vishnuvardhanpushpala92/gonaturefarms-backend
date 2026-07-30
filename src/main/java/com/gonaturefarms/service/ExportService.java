package com.gonaturefarms.service;

import com.gonaturefarms.entity.Order;
import com.gonaturefarms.entity.User;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Generates the CSV exports served by GET /api/admin/export/{type}. */
@Service
public class ExportService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ExportService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public record CsvFile(String filename, String content) {
    }

    @Transactional(readOnly = true)
    public CsvFile export(String type) {
        return switch (type) {
            case "orders" -> exportOrders();
            case "users" -> exportUsers();
            case "monthly" -> exportMonthly();
            default -> throw new ApiException("Unknown export type");
        };
    }

    private CsvFile exportOrders() {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        String[] headers = {"Order ID", "Customer", "Phone", "City", "Pincode", "Total", "Status", "Payment", "Date"};
        StringBuilder sb = new StringBuilder(String.join(",", headers)).append("\n");
        for (Order o : orders) {
            sb.append(csvRow(
                    o.getOrderId(), o.getCustomerName(), o.getPhone(), o.getCity(), o.getPincode(),
                    o.getTotal(), o.getStatus(), o.getPaymentStatus(), o.getCreatedAt()
            ));
        }
        return new CsvFile("orders.csv", sb.toString());
    }

    private CsvFile exportUsers() {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.customer)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        String[] headers = {"Name", "Phone", "Email", "Pincode", "Joined"};
        StringBuilder sb = new StringBuilder(String.join(",", headers)).append("\n");
        for (User u : users) {
            sb.append(csvRow(u.getName(), u.getPhone(), u.getEmail(), u.getPincode(), u.getCreatedAt()));
        }
        return new CsvFile("users.csv", sb.toString());
    }

    private CsvFile exportMonthly() {
        List<Order> orders = orderRepository.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, long[]> counts = new TreeMap<>(java.util.Collections.reverseOrder());
        Map<String, BigDecimal> revenue = new TreeMap<>();
        for (Order o : orders) {
            if (o.getCreatedAt() == null) continue;
            String month = o.getCreatedAt().format(fmt);
            counts.merge(month, new long[]{1}, (a, b) -> new long[]{a[0] + b[0]});
            revenue.merge(month, o.getTotal(), BigDecimal::add);
        }
        String[] headers = {"Month", "Orders", "Revenue"};
        StringBuilder sb = new StringBuilder(String.join(",", headers)).append("\n");
        for (Map.Entry<String, long[]> e : counts.entrySet()) {
            BigDecimal rev = revenue.getOrDefault(e.getKey(), BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
            sb.append(csvRow(e.getKey(), e.getValue()[0], rev));
        }
        return new CsvFile("monthly_report.csv", sb.toString());
    }

    private String csvRow(Object... values) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) row.append(',');
            String v = values[i] == null ? "" : String.valueOf(values[i]);
            row.append('"').append(v.replace("\"", "\"\"")).append('"');
        }
        return row.append('\n').toString();
    }
}
