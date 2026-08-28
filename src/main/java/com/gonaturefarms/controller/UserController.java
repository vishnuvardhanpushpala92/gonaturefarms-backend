package com.gonaturefarms.controller;

import com.gonaturefarms.dto.auth.UpdateProfileRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.security.SecurityUtils;
import com.gonaturefarms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for user profile operations. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ApiResponse getProfile() {
        long start = System.currentTimeMillis();
        ApiResponse response = ApiResponse.ok().with("user", SecurityUtils.requireCurrentUser());
        System.out.println("Account request took: " + (System.currentTimeMillis() - start) + "ms");
        return response;
    }

    @PutMapping("/profile")
    public ApiResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        // Get the ID from the current token, then pass it to the service
        Long userId = SecurityUtils.requireCurrentUser().id();
        return authService.updateProfile(userId, request);
    }
}