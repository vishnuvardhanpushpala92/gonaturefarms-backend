package com.gonaturefarms.service;

import com.gonaturefarms.dto.auth.AdminLoginRequest;
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
import com.gonaturefarms.exception.ResourceNotFoundException;
import com.gonaturefarms.repository.UserRepository;
import com.gonaturefarms.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Normalize phone number by removing all non-digit characters
     * This ensures consistent storage and lookup regardless of formatting
     */
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        // Remove all non-digit characters
        String normalized = phone.replaceAll("[^0-9]", "");
        // If it starts with country code (e.g., 91 for India), remove it if it results in 10 digits
        if (normalized.length() == 12 && normalized.startsWith("91")) {
            normalized = normalized.substring(2);
        }
        return normalized;
    }

    @Transactional
    public ApiResponse register(RegisterRequest request) {
        System.out.println("=== REGISTRATION DEBUG ===");
        System.out.println("Request object: " + request);
        System.out.println("Phone: " + request.getPhone());
        System.out.println("Username: " + request.getUsername());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Name: " + request.getName());
        System.out.println("Password: " + (request.getPassword() != null ? "present" : "null"));
        System.out.println("Security Question: " + request.getSecurityQuestion());
        System.out.println("Security Answer: " + request.getSecurityAnswer());
        System.out.println("==========================");

        // Normalize phone number
        String normalizedPhone = normalizePhone(request.getPhone());
        System.out.println("Normalized phone: " + normalizedPhone);

        // Additional validation
        if (normalizedPhone == null || normalizedPhone.length() != 10) {
            throw new ApiException("Mobile Number incorrect");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ApiException("Name incorrect");
        }
        
        // Strong password validation
        String password = request.getPassword();
        if (password == null || password.length() < 8) {
            throw new ApiException("Password incorrect");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ApiException("Password incorrect");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ApiException("Password incorrect");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new ApiException("Password incorrect");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new ApiException("Password incorrect");
        }

        // Check for existing phone number using normalized phone
        if (userRepository.existsByPhone(normalizedPhone)) {
            System.err.println("Registration failed: Phone number already registered - " + normalizedPhone);
            throw new ApiException("This phone number is already registered. Please log in or click Forgot Password.");
        }

        // Check for existing email (only if email is provided)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Basic email validation
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!request.getEmail().matches(emailRegex)) {
                throw new ApiException("Email incorrect");
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                System.err.println("Registration failed: Email already registered - " + request.getEmail());
                throw new ApiException("Email already registered");
            }
        }

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .phone(normalizedPhone)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.customer)
                .securityQuestion(request.getSecurityQuestion())
                .securityAnswer(request.getSecurityAnswer())
                .whatsappOptOut(false)
                .build();
        user = userRepository.save(user);
        String token = jwtService.generateToken(user);

        System.out.println("Registration successful for user ID: " + user.getId());
        return ApiResponse.ok("Registration successful")
                .with("token", token)
                .with("user", UserSummary.from(user));
    }

    @Transactional(readOnly = true)
    public ApiResponse login(LoginRequest request) {
        // Normalize identifier for phone numbers
        String normalizedIdentifier = request.getIdentifier();
        if (normalizedIdentifier != null && normalizedIdentifier.matches("^\\d+$")) {
            // It's a phone number (only digits), normalize it
            normalizedIdentifier = normalizePhone(normalizedIdentifier);
            System.out.println("Normalized phone for login: " + normalizedIdentifier);
        }
        
        User user = userRepository.findCustomerByIdentifier(normalizedIdentifier)
                .orElseThrow(() -> new ApiException("Invalid number"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid password");
        }
        String token = jwtService.generateToken(user);
        return ApiResponse.ok("Login successful")
                .with("token", token)
                .with("user", UserSummary.from(user));
    }

    @Transactional(readOnly = true)
    public ApiResponse adminLogin(AdminLoginRequest request) {
        User user = userRepository.findFirstByRoleAndIdentifier(User.UserRole.admin, request.getUsername())
                .orElseThrow(() -> new ApiException("Admin account not found. Please check your username."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Incorrect admin password. Please try again.");
        }
        String token = jwtService.generateToken(user);
        return ApiResponse.ok("Admin login successful")
                .with("token", token)
                .with("user", UserSummary.from(user));
    }

    @Transactional(readOnly = true)
    public ApiResponse me(Long userId) { // ✅ Changed to Long
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ApiResponse.ok().with("user", UserSummary.from(user));
    }

    @Transactional(readOnly = true)
    public ApiResponse verifySecurityQuestion(SecurityQuestionVerifyRequest request) {
        // Normalize identifier for phone numbers
        String normalizedIdentifier = request.getIdentifier();
        boolean isPhone = normalizedIdentifier != null && normalizedIdentifier.matches("^\\d+$");
        
        if (isPhone) {
            // It's a phone number (only digits), normalize it
            normalizedIdentifier = normalizePhone(normalizedIdentifier);
            System.out.println("Normalized phone for forgot password: " + normalizedIdentifier);
        }
        
        // Try to find user by phone or email using flexible matching
        User user = userRepository.findByPhoneOrEmailFlexible(isPhone ? normalizedIdentifier : null, isPhone ? null : request.getIdentifier())
                .orElseThrow(() -> new ApiException("If an account exists with this phone number or email, you will receive instructions to reset your password."));
        
        if (user.getSecurityQuestion() == null) {
            throw new ApiException("No security question set for this account");
        }
        return ApiResponse.ok("Security question fetched")
                .with("securityQuestion", user.getSecurityQuestion());
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        // Normalize identifier for phone numbers
        String normalizedIdentifier = request.getEmail();
        boolean isPhone = normalizedIdentifier != null && normalizedIdentifier.matches("^\\d+$");
        
        if (isPhone) {
            normalizedIdentifier = normalizePhone(normalizedIdentifier);
        }
        
        User user = userRepository.findByPhoneOrEmailFlexible(isPhone ? normalizedIdentifier : null, isPhone ? null : request.getEmail())
                .orElseThrow(() -> new ApiException("If an account exists, the security answer is incorrect"));
        if (!user.getSecurityAnswer().equalsIgnoreCase(request.getAnswer())) {
            throw new ApiException("Security Answer incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.ok("Password reset successfully");
    }

    @Transactional
    public ApiResponse resetPasswordWithSecurityQuestion(SecurityQuestionResetRequest request) {
        // Normalize identifier for phone numbers
        String normalizedIdentifier = request.getEmail();
        boolean isPhone = normalizedIdentifier != null && normalizedIdentifier.matches("^\\d+$");
        
        if (isPhone) {
            normalizedIdentifier = normalizePhone(normalizedIdentifier);
        }
        
        User user = userRepository.findByPhoneOrEmailFlexible(isPhone ? normalizedIdentifier : null, isPhone ? null : request.getEmail())
                .orElseThrow(() -> new ApiException("If an account exists, the security answer is incorrect"));
        if (!user.getSecurityAnswer().equalsIgnoreCase(request.getAnswer())) {
            throw new ApiException("Security Answer incorrect");
        }
        
        // Validate new password with same rules as registration
        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < 8) {
            throw new ApiException("Password must be at least 8 characters");
        }
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new ApiException("Password must contain at least one uppercase letter");
        }
        if (!newPassword.matches(".*[a-z].*")) {
            throw new ApiException("Password must contain at least one lowercase letter");
        }
        if (!newPassword.matches(".*[0-9].*")) {
            throw new ApiException("Password must contain at least one number");
        }
        if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new ApiException("Password must contain at least one special character");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ApiResponse.ok("Password reset successfully. Please login with your new password.");
    }

    @Transactional
    public ApiResponse forgotPassword(com.gonaturefarms.dto.auth.ForgotPasswordRequest request) {
        // Normalize identifier for phone numbers
        String normalizedIdentifier = request.getIdentifier();
        boolean isPhone = normalizedIdentifier != null && normalizedIdentifier.matches("^\\d+$");
        
        if (isPhone) {
            // It's a phone number (only digits), normalize it
            normalizedIdentifier = normalizePhone(normalizedIdentifier);
            System.out.println("Normalized phone for forgot password request: " + normalizedIdentifier);
        }
        
        // Generic response to prevent account enumeration
        // Always return success regardless of whether account exists
        userRepository.findByPhoneOrEmailFlexible(isPhone ? normalizedIdentifier : null, isPhone ? null : request.getIdentifier());
        return ApiResponse.ok("If an account exists with this phone number or email, you will receive instructions to reset your password.");
    }

    @Transactional
    public ApiResponse updateProfile(Long userId, UpdateProfileRequest request) { // ✅ Changed to Long
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPincode() != null) user.setPincode(request.getPincode());
        userRepository.save(user);
        return ApiResponse.ok("Profile updated").with("user", UserSummary.from(user));
    }
}