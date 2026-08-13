package com.gonaturefarms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.admin.SlideRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Slide;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.SlideRepository;

@Service
public class SlideService {

    private final SlideRepository slideRepository;

    public SlideService(SlideRepository slideRepository) {
        this.slideRepository = slideRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        return ApiResponse.ok().with("slides", slideRepository.findAllByOrderBySortOrderAscIdAsc());
    }

    @Transactional
    public ApiResponse create(SlideRequest req) {
        if (req.getImageUrl() == null || req.getImageUrl().isBlank()) {
            throw new ApiException("Image URL required");
        }
        Slide slide = Slide.builder()
                .imageUrl(req.getImageUrl())
                .caption(req.getCaption() == null ? "" : req.getCaption())
                .subText(req.getSubText() == null ? "" : req.getSubText())
                .sortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder())
                .build();
        slide = slideRepository.save(slide);
        return ApiResponse.ok("Slide added").with("id", slide.getId());
    }

    @Transactional
    public ApiResponse update(Long id, SlideRequest req) {
        Slide slide = slideRepository.findById(id)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Slide not found"));
        slide.setImageUrl(req.getImageUrl());
        slide.setCaption(req.getCaption() == null ? "" : req.getCaption());
        slide.setSubText(req.getSubText() == null ? "" : req.getSubText());
        slideRepository.save(slide);
        return ApiResponse.ok("Slide updated");
    }

    @Transactional
    public ApiResponse delete(Long id) {
        Slide slide = slideRepository.findById(id)
                .orElseThrow(() -> new com.gonaturefarms.exception.ResourceNotFoundException("Slide not found"));
        slideRepository.delete(slide);
        return ApiResponse.ok("Slide deleted");
    }
}
