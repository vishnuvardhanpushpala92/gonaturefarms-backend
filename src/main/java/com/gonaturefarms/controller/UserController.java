package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for user profile operations. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/profile")
    public ApiResponse getProfile() {
        return ApiResponse.ok().with("user", SecurityUtils.requireCurrentUser());
    }
}
