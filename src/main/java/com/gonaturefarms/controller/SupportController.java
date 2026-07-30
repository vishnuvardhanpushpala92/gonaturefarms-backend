package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.support.SupportTicketRequest;
import com.gonaturefarms.dto.support.SupportTicketUpdateRequest;
import com.gonaturefarms.service.SupportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** REST controller for customer support tickets. Mirrors routes/support.js. */
@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping
    public ApiResponse submit(@RequestBody SupportTicketRequest request) {
        return supportService.submit(request);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminAll() {
        return supportService.adminAll();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody SupportTicketUpdateRequest request) {
        return supportService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return supportService.delete(id);
    }
}
