package com.gonaturefarms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.HomepageService;

/**
 * Homepage data endpoint - batches all required homepage data in a single request
 * Reduces API calls from 9 to 1 for initial page load
 */
@RestController
@RequestMapping("/api/homepage")
public class HomepageController {

    private final HomepageService homepageService;

    public HomepageController(HomepageService homepageService) {
        this.homepageService = homepageService;
    }

    @GetMapping
    public ApiResponse getHomepageData() {
        return homepageService.getHomepageData();
    }
}
