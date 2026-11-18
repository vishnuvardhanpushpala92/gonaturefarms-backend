package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Policy;
import com.gonaturefarms.service.PolicyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/policies")
@PreAuthorize("hasRole('ADMIN')")
public class PolicyAdminController {
    private final PolicyService service;
    
    public PolicyAdminController(PolicyService service) {
        this.service = service;
    }
    
    @GetMapping
    public ApiResponse getAll() {
        return ApiResponse.ok().with("policies", service.getAll());
    }
    
    @PutMapping
    public ApiResponse update(@RequestBody Policy policy) {
        return ApiResponse.ok("Policy updated").with("policy", service.update(policy));
    }
}
