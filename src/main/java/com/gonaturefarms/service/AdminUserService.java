package com.gonaturefarms.service;

import com.gonaturefarms.dto.admin.CredentialsUpdateRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Order;
import com.gonaturefarms.entity.User;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Powers admin customer management (GET/DELETE /api/admin/users) and admin credential updates. */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, OrderRepository orderRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ApiResponse listCustomers() {
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.customer)
                .collect(Collectors.toList());

        List<Map<String, Object>> result = customers.stream().map(u -> {
            List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(u.getId());
            BigDecimal totalSpent = orders.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("pincode", u.getPincode());
            m.put("role", u.getRole());
            m.put("is_verified", u.getIsVerified());
            m.put("created_at", u.getCreatedAt());
            m.put("order_count", orders.size());
            m.put("total_spent", totalSpent);
            return m;
        }).sorted((a, b) -> ((java.time.LocalDateTime) b.get("created_at"))
                .compareTo((java.time.LocalDateTime) a.get("created_at")))
          .collect(Collectors.toList());

        return ApiResponse.ok().with("users", result);
    }

    @Transactional
    public ApiResponse deleteCustomer(Long id) {
        userRepository.findById(id)
                .filter(u -> u.getRole() != User.UserRole.admin)
                .ifPresent(userRepository::delete);
        return ApiResponse.ok("User deleted");
    }

    @Transactional
    public ApiResponse updateCredentials(CredentialsUpdateRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ApiException("Username and password required");
        }
        if (req.getPassword().length() < 6) {
            throw new ApiException("Password must be at least 6 chars");
        }
        // Mirrors the original upsert keyed on the fixed admin phone '0000000001'
        User admin = userRepository.findByPhone("0000000001")
                .orElseGet(() -> User.builder()
                        .phone("0000000001")
                        .role(User.UserRole.admin)
                        .isVerified(true)
                        .build());
        admin.setName(req.getUsername());
        admin.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(admin);
        return ApiResponse.ok("Admin credentials updated");
    }
}
