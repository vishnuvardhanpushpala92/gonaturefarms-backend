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

import com.gonaturefarms.dto.admin.SlideRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.SlideService;

/** GET /api/admin/slides (public) plus admin CRUD. */
@RestController
@RequestMapping("/api/admin/slides")
public class AdminSlideController {

    private final SlideService slideService;

    public AdminSlideController(SlideService slideService) {
        this.slideService = slideService;
    }

    @GetMapping
    public ApiResponse list() {
        return slideService.list();
    }

    @GetMapping("/admin-list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminList() {
        return slideService.listAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@RequestBody SlideRequest request) {
        return slideService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody SlideRequest request) {
        return slideService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return slideService.delete(id);
    }
}
