package com.gonaturefarms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.admin.FooterLinkRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.FooterLinkService;

/** GET /api/footer-links (public) plus admin CRUD. */
@RestController
@RequestMapping("/api/footer-links")
public class FooterLinkController {

    private final FooterLinkService footerLinkService;

    public FooterLinkController(FooterLinkService footerLinkService) {
        this.footerLinkService = footerLinkService;
    }

    @GetMapping
    public ApiResponse list() {
        return footerLinkService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@RequestBody FooterLinkRequest request) {
        return footerLinkService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody FooterLinkRequest request) {
        return footerLinkService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return footerLinkService.delete(id);
    }
}
