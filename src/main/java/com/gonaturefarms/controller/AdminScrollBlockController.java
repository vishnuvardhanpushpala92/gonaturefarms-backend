package com.gonaturefarms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.admin.ScrollBlockRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.ScrollBlockService;

/** GET /api/admin/scroll-content (public) plus admin CRUD. */
@RestController
@RequestMapping("/api/admin/scroll-content")
public class AdminScrollBlockController {

    private final ScrollBlockService scrollBlockService;

    public AdminScrollBlockController(ScrollBlockService scrollBlockService) {
        this.scrollBlockService = scrollBlockService;
    }

    @GetMapping
    public ApiResponse list() {
        return scrollBlockService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@RequestBody ScrollBlockRequest request) {
        return scrollBlockService.create(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return scrollBlockService.delete(id);
    }
}
