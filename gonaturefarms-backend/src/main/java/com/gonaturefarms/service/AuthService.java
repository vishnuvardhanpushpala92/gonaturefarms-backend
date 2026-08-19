package com.gonaturefarms.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.auth.AdminLoginRequest;
import com.gonaturefarms.dto.auth.ForgotPasswordRequest;
import com.gonaturefarms.dto.auth.LoginRequest;
import com.gonaturefarms.dto.auth.RegisterRequest;
import com.gonaturefarms.dto.auth.ResetPasswordRequest;
import com.gonaturefarms.dto.auth.SecurityQuestionResetRequest;
import com.gonaturefarms.dto.auth.SecurityQuestionVerifyRequest;
import com.gonaturefarms.dto.auth.UpdateProfileRequest;
import com.gonaturefarms.dto.auth.UserSummary;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.User;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.DeliveryZoneRepository;
import com.gonaturefarms.repository.OrderRepository;
import com.gonaturefarms.repository.UserRepository;
import com.gonaturefarms.security.CurrentUser;
import com.gonaturefarms.security.JwtService;

/**
 * Business logic for account registration, login, admin login, and the "/me" profile
 * lookup. Mirrors routes/auth.js exactly, including its permissive "soft-fail"
 * validation style (business rule failures return {success:false,message} rather
 * than throwing hard errors).
 */
@Service
public class AuthService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.admin.default-user:Vishnu}")
    private String defaultAdminUser;

    @Value("${app.admin.default-pass:918252}")
    private String defaultAdminPass;

    public AuthService(UserRepository userRepository,
                        DeliveryZoneRepository deliveryZoneRepository,
                        OrderRepository orderRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public ApiResponse register(RegisterRequest req) {
        if (isBlank(req.getPhone()) || isBlank(req.getName()) || isBlank(req.getPassword())) {
            throw new ApiException("Name, phone and password are required");
        }
        if (!PHONE_PATTERN.matcher(req.getPhone().trim()).matches()) {
            throw new ApiException("Enter a valid 10-digit phone number");
        }
        if (req.getPassword().length() < 6) {
            throw new ApiException("Password must be at least 6 characters");
        }
        // Simple puzzle verification (human check) — matches the frontend's math puzzle widget
        if (req.getPuzzleAnswer() == null || !req.getPuzzleAnswer().equals("5")) {
            throw new ApiException("Incorrect puzzle answer. Please solve the puzzle correctly.");
        }
        if (userRepository.existsByPhone(req.getPhone().trim())) {
            throw new ApiException("Phone already registered. Please sign in.");
        }

        // Delivery zone check (only enforced once at least one zone is configured)
        if (!isBlank(req.getPincode())) {
            long zoneCount = deliveryZoneRepository.count();
            boolean zoneKnown = deliveryZoneRepository.findByPincode(req.getPincode().trim()).isPresent();
            if (zoneCount > 0 && !zoneKnown) {
                throw new ApiException("Sorry, we don't deliver to pincode " + req.getPincode() +
                        " yet. Contact us to request delivery.");
            }
        }

        User user = User.builder()
                .name(req.getName())
                .phone(req.getPhone().trim())
                .email(isBlank(req.getEmail()) ? null : req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .pincode(isBlank(req.getPincode()) ? null : req.getPincode())
                .securityQuestion(req.getSecurityQuestion())
                .securityAnswer(req.getSecurityAnswer() != null ? passwordEncoder.encode(req.getSecurityAnswer()) : null)
                .role(User.UserRole.customer)
                .isVerified(true)
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        return ApiResponse.ok("Welcome to Go Nature Farms, " + user.getName() + "! \uD83C\uDF3F")
                .with("token", token)
                .with("user", toSummary(user));
    }

    @Transactional(readOnly = true)
    public ApiResponse login(LoginRequest req) {
        if (isBlank(req.getIdentifier()) || isBlank(req.getPassword())) {
            throw new ApiException("Phone/email and password are required");
        }
        String identifier = req.getIdentifier().trim();
        User user = userRepository.findByPhoneOrEmail(identifier, identifier)
                .orElseThrow(() -> new ApiException("Account not found. Please register."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Incorrect password.");
        }

        String token = jwtService.generateToken(user);
        return ApiResponse.ok("Welcome back, " + user.getName() + "! \uD83C\uDF3F")
                .with("token", token)
                .with("user", toSummary(user));
    }

    @Transactional(readOnly = true)
    public ApiResponse adminLogin(AdminLoginRequest req) {
        if (isBlank(req.getUsername()) || isBlank(req.getPassword())) {
            throw new ApiException("Username and password required");
        }
        String username = req.getUsername().trim();

        // 1) Try DB admin rows first
        User adminUser = userRepository.findFirstByRoleAndIdentifier(User.UserRole.admin, username)
                .filter(u -> passwordEncoder.matches(req.getPassword(), u.getPasswordHash()))
                .orElse(null);

        // 2) Fallback: configured default admin credentials (id=0 sentinel, no DB row)
        UserSummary summary;
        if (adminUser != null) {
            summary = toSummary(adminUser);
        } else if (username.equals(defaultAdminUser) && req.getPassword().equals(defaultAdminPass)) {
            summary = UserSummary.builder().id(0L).name(defaultAdminUser).role("admin").build();
        } else {
            throw new ApiException("Invalid admin credentials");
        }

        String token = jwtService.generateToken(summary.getId(), summary.getName(), null, null, "admin");
        return ApiResponse.ok("Admin access granted. Welcome, " + summary.getName() + "!")
                .with("token", token)
                .with("user", UserSummary.builder().id(summary.getId()).name(summary.getName()).role("admin").build());
    }

    @Transactional(readOnly = true)
    public ApiResponse me(CurrentUser currentUser) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("User not found"));

        List<com.gonaturefarms.entity.Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long totalOrders = orders.size();
        BigDecimal totalSpent = orders.stream().map(com.gonaturefarms.entity.Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long delivered = orders.stream()
                .filter(o -> o.getStatus() == com.gonaturefarms.entity.Order.OrderStatus.Delivered)
                .count();

        return ApiResponse.ok()
                .with("user", user)
                .with("stats", java.util.Map.of(
                        "total_orders", totalOrders,
                        "total_spent", totalSpent,
                        "delivered", delivered));
    }

    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest req) {
        if (isBlank(req.getIdentifier())) {
            throw new ApiException("Phone or email is required");
        }
        String identifier = req.getIdentifier().trim();
        User user = userRepository.findByPhoneOrEmail(identifier, identifier)
                .orElse(null);

        if (user == null) {
            // For security, always return success even if user doesn't exist
            return ApiResponse.ok("If an account exists with this phone/email, a reset code has been sent.");
        }

        // Generate 6-digit reset code
        String resetCode = String.format("%06d", RANDOM.nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        user.setResetCode(resetCode);
        user.setResetCodeExpiresAt(expiresAt);
        userRepository.save(user);

        // In production, send SMS/email with the reset code
        // For now, return the code in response (for development only)
        return ApiResponse.ok("Password reset code: " + resetCode + " (valid for 15 minutes)")
                .with("code", resetCode);
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest req) {
        if (isBlank(req.getIdentifier()) || isBlank(req.getCode()) || isBlank(req.getNewPassword())) {
            throw new ApiException("Identifier, code and new password are required");
        }
        if (req.getNewPassword().length() < 6) {
            throw new ApiException("Password must be at least 6 characters");
        }

        String identifier = req.getIdentifier().trim();
        User user = userRepository.findByPhoneOrEmail(identifier, identifier)
                .orElseThrow(() -> new ApiException("Account not found"));

        if (user.getResetCode() == null || user.getResetCodeExpiresAt() == null) {
            throw new ApiException("No reset code requested. Please request a password reset first.");
        }

        if (LocalDateTime.now().isAfter(user.getResetCodeExpiresAt())) {
            throw new ApiException("Reset code has expired. Please request a new one.");
        }

        if (!user.getResetCode().equals(req.getCode())) {
            throw new ApiException("Invalid reset code");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiresAt(null);
        userRepository.save(user);

        return ApiResponse.ok("Password reset successfully. Please login with your new password.");
    }

    @Transactional(readOnly = true)
    public ApiResponse verifySecurityQuestion(SecurityQuestionVerifyRequest req) {
        if (isBlank(req.getIdentifier())) {
            throw new ApiException("Phone or email is required");
        }
        String identifier = req.getIdentifier().trim();
        User user = userRepository.findByPhoneOrEmail(identifier, identifier)
                .orElseThrow(() -> new ApiException("Account not found"));

        if (isBlank(user.getSecurityQuestion())) {
            throw new ApiException("No security question set for this account. Please contact support.");
        }

        return ApiResponse.ok("Security question found")
                .with("securityQuestion", user.getSecurityQuestion());
    }

    @Transactional
    public ApiResponse resetPasswordWithSecurityQuestion(SecurityQuestionResetRequest req) {
        if (isBlank(req.getIdentifier()) || isBlank(req.getSecurityAnswer()) || 
            isBlank(req.getNewPassword()) || isBlank(req.getConfirmPassword())) {
            throw new ApiException("All fields are required");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("Passwords do not match");
        }
        if (req.getNewPassword().length() < 6) {
            throw new ApiException("Password must be at least 6 characters");
        }

        String identifier = req.getIdentifier().trim();
        User user = userRepository.findByPhoneOrEmail(identifier, identifier)
                .orElseThrow(() -> new ApiException("Account not found"));

        if (isBlank(user.getSecurityQuestion()) || isBlank(user.getSecurityAnswer())) {
            throw new ApiException("No security question set for this account. Please contact support.");
        }

        if (!passwordEncoder.matches(req.getSecurityAnswer(), user.getSecurityAnswer())) {
            throw new ApiException("Incorrect security answer");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.ok("Password reset successfully. Please login with your new password.");
    }

    @Transactional
    public ApiResponse updateProfile(CurrentUser currentUser, UpdateProfileRequest req) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ApiException("User not found"));

        if (!isBlank(req.getName())) {
            user.setName(req.getName());
        }
        if (!isBlank(req.getPhone())) {
            if (!PHONE_PATTERN.matcher(req.getPhone()).matches()) {
                throw new ApiException("Phone must be 10 digits");
            }
            // Check if phone is already taken by another user
            userRepository.findByPhone(req.getPhone()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new ApiException("Phone number already in use");
                }
            });
            user.setPhone(req.getPhone());
        }
        if (!isBlank(req.getEmail())) {
            user.setEmail(req.getEmail());
        }
        if (!isBlank(req.getPincode())) {
            user.setPincode(req.getPincode());
        }

        userRepository.save(user);

        return ApiResponse.ok("Profile updated successfully")
                .with("user", toSummary(user));
    }

    private UserSummary toSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
