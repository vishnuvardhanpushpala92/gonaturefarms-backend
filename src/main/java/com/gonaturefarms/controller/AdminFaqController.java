package com.gonaturefarms.controller;

import com.gonaturefarms.dto.admin.FaqRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.FaqService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** GET /api/admin/faqs (public) plus admin CRUD. */
@RestController
@RequestMapping("/api/admin/faqs")
public class AdminFaqController {

    private final FaqService faqService;

    public AdminFaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public ApiResponse list() {
        return faqService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@RequestBody FaqRequest request) {
        return faqService.create(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return faqService.delete(id);
    }
}
