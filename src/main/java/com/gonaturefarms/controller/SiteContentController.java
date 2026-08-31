package com.gonaturefarms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.admin.SiteContentRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.SiteContentService;

/** GET /api/site-content (public) plus admin CRUD. */
@RestController
@RequestMapping("/api/site-content")
public class SiteContentController {

    private final SiteContentService siteContentService;

    public SiteContentController(SiteContentService siteContentService) {
        this.siteContentService = siteContentService;
    }

    @GetMapping
    public ApiResponse getBySlug(@RequestParam String slug) {
        return siteContentService.getBySlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@RequestBody SiteContentRequest request) {
        return siteContentService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody SiteContentRequest request) {
        return siteContentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return siteContentService.delete(id);
    }
}
