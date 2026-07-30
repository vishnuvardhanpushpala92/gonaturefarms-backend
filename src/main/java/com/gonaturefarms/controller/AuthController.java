package com.gonaturefarms.controller;

import com.gonaturefarms.dto.auth.AdminLoginRequest;
import com.gonaturefarms.dto.auth.LoginRequest;
import com.gonaturefarms.dto.auth.RegisterRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** REST controller for authentication. Mirrors routes/auth.js. */
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
        return authService.me(SecurityUtils.requireCurrentUser());
    }
}
