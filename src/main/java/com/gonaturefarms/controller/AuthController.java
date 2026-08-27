package com.gonaturefarms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.auth.AdminLoginRequest;
import com.gonaturefarms.dto.auth.ForgotPasswordRequest;
import com.gonaturefarms.dto.auth.LoginRequest;
import com.gonaturefarms.dto.auth.RegisterRequest;
import com.gonaturefarms.dto.auth.ResetPasswordRequest;
import com.gonaturefarms.dto.auth.SecurityQuestionResetRequest;
import com.gonaturefarms.dto.auth.SecurityQuestionVerifyRequest;
import com.gonaturefarms.dto.auth.UpdateProfileRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/admin-login")
    public ApiResponse adminLogin(@RequestBody AdminLoginRequest request) {
        return authService.adminLogin(request);
    }

    @GetMapping("/me")
    public ApiResponse me() {
        // ✅ FIX: Pass Long ID instead of CurrentUser
        return authService.me(SecurityUtils.requireCurrentUser().id());
    }

    @PostMapping("/forgot-password")
    public ApiResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/forgot-password/verify")
    public ApiResponse verifySecurityQuestion(@Valid @RequestBody SecurityQuestionVerifyRequest request) {
        return authService.verifySecurityQuestion(request);
    }

    @PostMapping("/reset-password/security-question")
    public ApiResponse resetPasswordWithSecurityQuestion(@Valid @RequestBody SecurityQuestionResetRequest request) {
        return authService.resetPasswordWithSecurityQuestion(request);
    }

    @PutMapping("/profile")
    public ApiResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        // ✅ FIX: Pass Long ID instead of CurrentUser
        return authService.updateProfile(SecurityUtils.requireCurrentUser().id(), request);
    }
}