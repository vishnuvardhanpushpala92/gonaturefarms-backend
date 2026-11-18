package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.SupportSettings;
import com.gonaturefarms.service.SupportSettingsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/support-settings")
@PreAuthorize("hasRole('ADMIN')")
public class SupportSettingsController {
    private final SupportSettingsService service;
    
    public SupportSettingsController(SupportSettingsService service) {
        this.service = service;
    }
    
    @GetMapping
    public ApiResponse getSettings() {
        return ApiResponse.ok().with("settings", service.getSettings());
    }
    
    @PutMapping
    public ApiResponse updateSettings(@RequestBody SupportSettings settings) {
        return ApiResponse.ok("Support settings updated").with("settings", service.update(settings));
    }
}
