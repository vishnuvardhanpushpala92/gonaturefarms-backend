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

    @Transactional
    public ApiResponse register(RegisterRequest request) {
        System.out.println("Registration attempt for phone: " + request.getPhone());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Name: " + request.getName());
        
        if (userRepository.existsByPhone(request.getPhone())) {
            System.err.println("Registration failed: Phone number already registered - " + request.getPhone());
            throw new ApiException("Phone number already registered");
        }
        
        User user = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
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
        User user = userRepository.findByPhoneOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new ApiException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid credentials");
        }
        String token = jwtService.generateToken(user);
        return ApiResponse.ok("Login successful")
                .with("token", token)
                .with("user", UserSummary.from(user));
    }

    @Transactional(readOnly = true)
    public ApiResponse adminLogin(AdminLoginRequest request) {
        User user = userRepository.findFirstByRoleAndIdentifier(User.UserRole.admin, request.getUsername())
                .orElseThrow(() -> new ApiException("Invalid admin credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid admin credentials");
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
        // ✅ FIX: Use ApiException (-> HTTP 200, success:false) instead of
        // ResourceNotFoundException (-> HTTP 404) so a "no such account" result is
        // handled the same friendly way as every other forgot-password outcome,
        // instead of surfacing as a raw 404 in the browser console.
        User user = userRepository.findByPhoneOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new ApiException("No account found with that phone number or email"));
        if (user.getSecurityQuestion() == null) {
            throw new ApiException("No security question set for this account");
        }
        return ApiResponse.ok("Security question fetched")
                .with("securityQuestion", user.getSecurityQuestion());
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPhoneOrEmail(request.getEmail(), request.getEmail())
                .orElseThrow(() -> new ApiException("No account found with that phone number or email"));
        if (!user.getSecurityAnswer().equalsIgnoreCase(request.getAnswer())) {
            throw new ApiException("Incorrect security answer");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.ok("Password reset successfully");
    }

    @Transactional
    public ApiResponse resetPasswordWithSecurityQuestion(SecurityQuestionResetRequest request) {
        User user = userRepository.findByPhoneOrEmail(request.getEmail(), request.getEmail()) // ✅ Changed to getEmail()
                .orElseThrow(() -> new ApiException("No account found with that phone number or email"));
        if (!user.getSecurityAnswer().equalsIgnoreCase(request.getAnswer())) { // ✅ Changed to getAnswer()
            throw new ApiException("Incorrect security answer");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ApiResponse.ok("Password reset successfully");
    }

    @Transactional
    public ApiResponse forgotPassword(com.gonaturefarms.dto.auth.ForgotPasswordRequest request) {
        userRepository.findByPhoneOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new ApiException("No account found with that phone number or email"));
        return ApiResponse.ok("If the account exists, a reset process will begin");
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