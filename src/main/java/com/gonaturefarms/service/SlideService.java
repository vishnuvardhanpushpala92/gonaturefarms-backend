package com.gonaturefarms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.admin.SlideRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Slide;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.SlideRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlideService {

    private final SlideRepository slideRepository;

    public SlideService(SlideRepository slideRepository) {
        this.slideRepository = slideRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        List<Slide> allSlides = slideRepository.findAllByOrderBySortOrderAscIdAsc();
        // Slides are no longer pending by default, so show all active slides
        return ApiResponse.ok().with("slides", allSlides);
    }

    @Transactional(readOnly = true)
    public ApiResponse listAll() {
        List<Slide> allSlides = slideRepository.findAllByOrderBySortOrderAscIdAsc();
        // Include pending slides for admin view
        return ApiResponse.ok().with("slides", allSlides);
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
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder().intValue() : 0)
                .pending(false)  // Changed to false so slides appear immediately
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
        slide.setPending(false);  // Changed to false for immediate visibility
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
