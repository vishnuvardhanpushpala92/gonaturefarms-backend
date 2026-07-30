package com.gonaturefarms.controller;

import com.gonaturefarms.dto.admin.SlideRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.SlideService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
