package com.gonaturefarms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.admin.FooterLinkRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.FooterLink;
import com.gonaturefarms.entity.FooterLink.LinkCategory;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.FooterLinkRepository;

@Service
public class FooterLinkService {

    private final FooterLinkRepository footerLinkRepository;

    public FooterLinkService(FooterLinkRepository footerLinkRepository) {
        this.footerLinkRepository = footerLinkRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        List<FooterLink> links = footerLinkRepository.findAllByOrderBySortOrderAscIdAsc();
        return ApiResponse.ok().with("links", links);
    }

    @Transactional
    public ApiResponse create(FooterLinkRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ApiException("Link name is required");
        }
        if (req.getUrl() == null || req.getUrl().isBlank()) {
            throw new ApiException("Link URL is required");
        }
        
        // Parse category
        LinkCategory category = parseCategory(req.getCategory());
        
        FooterLink link = FooterLink.builder()
                .name(req.getName())
                .url(req.getUrl())
                .category(category)
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .build();
        link = footerLinkRepository.save(link);
        return ApiResponse.ok("Footer link added successfully").with("id", link.getId());
    }

    @Transactional
    public ApiResponse update(Long id, FooterLinkRequest req) {
        FooterLink link = footerLinkRepository.findById(id)
                .orElseThrow(() -> new ApiException("Footer link not found"));
        
        if (req.getName() != null && !req.getName().isBlank()) {
            link.setName(req.getName());
        }
        if (req.getUrl() != null && !req.getUrl().isBlank()) {
            link.setUrl(req.getUrl());
        }
        if (req.getCategory() != null) {
            link.setCategory(parseCategory(req.getCategory()));
        }
        if (req.getSortOrder() != null) {
            link.setSortOrder(req.getSortOrder());
        }
        
        link.setUpdatedAt(java.time.LocalDateTime.now());
        link = footerLinkRepository.save(link);
        return ApiResponse.ok("Footer link updated successfully");
    }

    @Transactional
    public ApiResponse delete(Long id) {
        if (!footerLinkRepository.existsById(id)) {
            throw new ApiException("Footer link not found");
        }
        footerLinkRepository.deleteById(id);
        return ApiResponse.ok("Footer link deleted successfully");
    }

    private LinkCategory parseCategory(String category) {
        if (category == null || category.isBlank()) return LinkCategory.QUICK_LINKS;
        try {
            return LinkCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LinkCategory.QUICK_LINKS;
        }
    }
}
