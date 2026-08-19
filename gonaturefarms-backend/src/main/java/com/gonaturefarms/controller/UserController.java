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
        return ApiResponse.ok().with("user", SecurityUtils.requireCurrentUser());
    }

    @PutMapping("/profile")
    public ApiResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(SecurityUtils.requireCurrentUser(), request);
    }
}
