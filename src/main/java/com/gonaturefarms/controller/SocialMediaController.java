package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.SocialMediaLink;
import com.gonaturefarms.service.SocialMediaLinkService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/social-media")
@PreAuthorize("hasRole('ADMIN')")
public class SocialMediaController {
    private final SocialMediaLinkService service;
    
    public SocialMediaController(SocialMediaLinkService service) {
        this.service = service;
    }
    
    @GetMapping
    public ApiResponse getAll() {
        return ApiResponse.ok().with("links", service.getAll());
    }
    
    @PostMapping
    public ApiResponse create(@RequestBody SocialMediaLink link) {
        return ApiResponse.ok("Social media link created").with("link", service.create(link));
    }
    
    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody SocialMediaLink link) {
        return ApiResponse.ok("Social media link updated").with("link", service.update(id, link));
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok("Social media link deleted");
    }
}
