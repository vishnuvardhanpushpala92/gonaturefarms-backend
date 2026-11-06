package com.gonaturefarms.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.homepage.HomepageLayoutRequest;
import com.gonaturefarms.service.HomepageLayoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    private static final Logger log = LoggerFactory.getLogger(HomepageLayoutController.class);

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
        log.info("Saving homepage layout for user: {}", username);
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

    /**
     * Handle JSON parsing errors with detailed logging
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse handleJsonErrors(HttpMessageNotReadableException ex) {
        log.error("=== Homepage Layout JSON Parsing Error ===");
        log.error("Exception type: {}", ex.getClass().getName());
        log.error("Exception message: {}", ex.getMessage());

        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            log.error("Invalid format for field: {}", ife.getPath());
            log.error("Field value: {}", ife.getValue());
            log.error("Target type: {}", ife.getTargetType().getName());
            return ApiResponse.fail("Invalid format for field: " + ife.getPath() + ". Expected: " + ife.getTargetType().getSimpleName());
        }

        if (ex.getCause() instanceof JsonMappingException) {
            JsonMappingException jme = (JsonMappingException) ex.getCause();
            log.error("JSON mapping error: {}", jme.getMessage());
            log.error("Path: {}", jme.getPath());
            return ApiResponse.fail("JSON mapping error: " + jme.getMessage());
        }

        log.error("Full exception:", ex);
        return ApiResponse.fail("Malformed request body: " + ex.getMostSpecificCause().getMessage());
    }
}
