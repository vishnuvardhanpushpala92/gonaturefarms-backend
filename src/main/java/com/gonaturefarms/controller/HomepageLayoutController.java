package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.homepage.HomepageLayoutRequest;
import com.gonaturefarms.service.HomepageLayoutService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for homepage layout configuration
 * Admin-only endpoints for managing layout settings
 */
@RestController
@RequestMapping("/api/homepage-layout")
public class HomepageLayoutController {

    private final HomepageLayoutService homepageLayoutService;

    public HomepageLayoutController(HomepageLayoutService homepageLayoutService) {
        this.homepageLayoutService = homepageLayoutService;
    }

    /**
     * Get published layout (public endpoint)
     */
    @GetMapping("/published")
    public ApiResponse getPublishedLayout() {
        return homepageLayoutService.getPublishedLayout();
    }

    /**
     * Get latest layout (admin only)
     */
    @GetMapping("/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse getLatestLayout() {
        return homepageLayoutService.getLatestLayout();
    }

    /**
     * Save layout (admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse saveLayout(@RequestBody HomepageLayoutRequest request, Authentication authentication) {
        String username = authentication.getName();
        return homepageLayoutService.saveLayout(request, username);
    }

    /**
     * Reset to defaults (admin only)
     */
    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse resetToDefaults(Authentication authentication) {
        String username = authentication.getName();
        return homepageLayoutService.resetToDefaults(username);
    }
}
