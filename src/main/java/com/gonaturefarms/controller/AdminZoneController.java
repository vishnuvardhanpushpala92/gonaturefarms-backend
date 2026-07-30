package com.gonaturefarms.controller;

import com.gonaturefarms.dto.admin.ZoneRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.DeliveryZoneService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
