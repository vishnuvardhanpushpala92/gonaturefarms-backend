package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.SupportSettings;
import com.gonaturefarms.service.SupportSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public read-only support contact settings for the customer care popup. */
@RestController
@RequestMapping("/api/support-settings")
public class PublicSupportSettingsController {

    private final SupportSettingsService service;

    public PublicSupportSettingsController(SupportSettingsService service) {
        this.service = service;
    }

    @GetMapping("/public")
    public ApiResponse publicSettings() {
        SupportSettings s = service.getSettings();
        return ApiResponse.ok().with("settings", s);
    }
}
