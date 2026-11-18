package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.support.SupportTicketRequest;
import com.gonaturefarms.dto.support.SupportTicketUpdateRequest;
import com.gonaturefarms.service.SupportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/my-tickets")
    public ApiResponse getMyTickets(Authentication authentication) {
        // Customer can only view their own tickets
        Long userId = Long.parseLong(authentication.getName());
        return supportService.getTicketsByUserId(userId);
    }

    @GetMapping("/my-tickets/{id}")
    public ApiResponse getMyTicket(@PathVariable Long id, Authentication authentication) {
        // Customer can only view their own ticket
        Long userId = Long.parseLong(authentication.getName());
        return supportService.getTicketByIdAndUserId(id, userId);
    }

    @GetMapping("/admin/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminAll() {
        return supportService.adminAll();
    }

    @GetMapping("/admin/tickets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminGetTicket(@PathVariable Long id) {
        return supportService.getTicketById(id);
    }

    @PutMapping("/admin/tickets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody SupportTicketUpdateRequest request) {
        return supportService.update(id, request);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse adminAllLegacy() {
        return supportService.adminAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return supportService.delete(id);
    }
}
