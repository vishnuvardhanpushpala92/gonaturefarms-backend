package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/health — simple liveness check, equivalent to the Express health route. */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse health() {
        return ApiResponse.ok("Go Nature Farms API is running").with("timestamp", java.time.Instant.now());
    }
}
