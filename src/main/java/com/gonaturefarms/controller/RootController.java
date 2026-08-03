package com.gonaturefarms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.common.ApiResponse;

/**
 * Root endpoint controller - returns API status when accessing the base URL.
 * Equivalent to Express: app.get("/", (req, res) => { res.status(200).json({ success: true, message: "GoNature Farms API is running" }); });
 */
@RestController
@RequestMapping("/")
public class RootController {

    @GetMapping
    public ApiResponse root() {
        return ApiResponse.ok("GoNature Farms API is running")
                .with("version", "1.0.0")
                .with("endpoints", new String[]{
                        "/api/health",
                        "/api/products",
                        "/api/products/categories",
                        "/api/videos",
                        "/api/reviews/home/featured",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/admin/settings/public",
                        "/api/admin/slides",
                        "/api/admin/faqs",
                        "/api/admin/zones",
                        "/api/admin/scroll-content"
                });
    }
}
