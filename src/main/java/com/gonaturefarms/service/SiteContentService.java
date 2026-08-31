package com.gonaturefarms.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.admin.SiteContentRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.SiteContent;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.SiteContentRepository;

@Service
public class SiteContentService {

    private final SiteContentRepository siteContentRepository;

    public SiteContentService(SiteContentRepository siteContentRepository) {
        this.siteContentRepository = siteContentRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse getBySlug(String slug) {
        Optional<SiteContent> content = siteContentRepository.findBySlug(slug);
        if (content.isEmpty()) {
            // Return default structure to prevent frontend null errors
            return ApiResponse.ok().with("content", getDefaultContent(slug));
        }
        // Don't return pending content to public (treat NULL as false)
        if (content.get().getPending() != null && content.get().getPending()) {
            return ApiResponse.ok().with("content", getDefaultContent(slug));
        }
        return ApiResponse.ok().with("content", content.get());
    }

    @Transactional(readOnly = true)
    public ApiResponse getBySlugAdmin(String slug) {
        Optional<SiteContent> content = siteContentRepository.findBySlug(slug);
        if (content.isEmpty()) {
            // Return default structure to prevent frontend null errors
            return ApiResponse.ok().with("content", getDefaultContent(slug));
        }
        // Include pending content for admin view
        return ApiResponse.ok().with("content", content.get());
    }

    @Transactional
    public ApiResponse create(SiteContentRequest req) {
        if (req.getSlug() == null || req.getSlug().isBlank()) {
            throw new ApiException("Slug is required");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new ApiException("Title is required");
        }
        
        if (siteContentRepository.existsBySlug(req.getSlug())) {
            throw new ApiException("Content with this slug already exists");
        }
        
        SiteContent content = SiteContent.builder()
                .slug(req.getSlug())
                .title(req.getTitle())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .personName(req.getPersonName())
                .personRole(req.getPersonRole())
                .personImageUrl(req.getPersonImageUrl())
                .optionalLink(req.getOptionalLink())
                .pending(true)
                .build();
        content = siteContentRepository.save(content);
        return ApiResponse.ok("Site content created successfully").with("id", content.getId());
    }

    @Transactional
    public ApiResponse update(Long id, SiteContentRequest req) {
        SiteContent content = siteContentRepository.findById(id)
                .orElseThrow(() -> new ApiException("Site content not found"));
        
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            content.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            content.setDescription(req.getDescription());
        }
        if (req.getImageUrl() != null) {
            content.setImageUrl(req.getImageUrl());
        }
        if (req.getPersonName() != null) {
            content.setPersonName(req.getPersonName());
        }
        if (req.getPersonRole() != null) {
            content.setPersonRole(req.getPersonRole());
        }
        if (req.getPersonImageUrl() != null) {
            content.setPersonImageUrl(req.getPersonImageUrl());
        }
        if (req.getOptionalLink() != null) {
            content.setOptionalLink(req.getOptionalLink());
        }

        content.setPending(true);
        content.setUpdatedAt(java.time.LocalDateTime.now());
        content = siteContentRepository.save(content);
        return ApiResponse.ok("Site content updated successfully");
    }

    @Transactional
    public ApiResponse delete(Long id) {
        if (!siteContentRepository.existsById(id)) {
            throw new ApiException("Site content not found");
        }
        siteContentRepository.deleteById(id);
        return ApiResponse.ok("Site content deleted successfully");
    }

    private SiteContent getDefaultContent(String slug) {
        return SiteContent.builder()
                .slug(slug)
                .title("")
                .description("")
                .imageUrl("")
                .personName("")
                .personRole("")
                .personImageUrl("")
                .build();
    }
}
