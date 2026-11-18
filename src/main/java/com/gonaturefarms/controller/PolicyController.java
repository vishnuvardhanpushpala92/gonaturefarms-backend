package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Policy;
import com.gonaturefarms.service.PolicyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private final PolicyService service;
    
    public PolicyController(PolicyService service) {
        this.service = service;
    }
    
    @GetMapping("/{type}")
    public ApiResponse getByType(@PathVariable String type) {
        return ApiResponse.ok().with("policy", service.getByType(type));
    }
}
