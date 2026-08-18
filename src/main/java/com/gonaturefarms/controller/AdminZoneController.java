package com.gonaturefarms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.admin.ZoneRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.DeliveryZoneService;

/** GET /api/admin/zones (public) plus admin CRUD. */
@RestController
@RequestMapping("/api/admin/zones")
public class AdminZoneController {

    private final DeliveryZoneService deliveryZoneService;

    public AdminZoneController(DeliveryZoneService deliveryZoneService) {
        this.deliveryZoneService = deliveryZoneService;
    }

    @GetMapping
    public ApiResponse list() {
        return deliveryZoneService.list();
    }

    @GetMapping("/validate")
    public ApiResponse validatePincode(@RequestParam String pincode) {
        return deliveryZoneService.validatePincode(pincode);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse upsert(@RequestBody ZoneRequest request) {
        return deliveryZoneService.upsert(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return deliveryZoneService.delete(id);
    }
}
