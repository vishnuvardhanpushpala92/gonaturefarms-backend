package com.gonaturefarms.controller;

import com.gonaturefarms.dto.admin.ScrollBlockRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.ScrollBlockService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
