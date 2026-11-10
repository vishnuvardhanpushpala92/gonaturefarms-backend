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
    private final HomepageService homepageService;

    public SlideService(SlideRepository slideRepository, HomepageService homepageService) {
        this.slideRepository = slideRepository;
        this.homepageService = homepageService;
    }

    @Transactional(readOnly = true)
    public ApiResponse list() {
        try {
            List<Slide> allSlides = slideRepository.findAllByOrderBySortOrderAscIdAsc();
            // Slides are no longer pending by default, so show all active slides
            return ApiResponse.ok().with("slides", allSlides);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Slide Service.list() !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error loading slides: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse listAll() {
        try {
            List<Slide> allSlides = slideRepository.findAllByOrderBySortOrderAscIdAsc();
            // Include pending slides for admin view
            return ApiResponse.ok().with("slides", allSlides);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Slide Service.listAll() !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error loading admin slides: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse create(SlideRequest request) {
        try {
            Slide slide = new Slide();
            slide.setCaption(request.getCaption());
            slide.setSubText(request.getSubText());
            slide.setImageUrl(request.getImageUrl());
            slide.setSortOrder(request.getSortOrder());
            slide.setPending(false); // Default to false for immediate visibility
            
            Slide saved = slideRepository.save(slide);
            
            // Evict homepage cache after content update
            homepageService.evictHomepageCache();
            
            return ApiResponse.ok("Slide created successfully").with("slide", saved);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Slide Service.create() !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error creating slide: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse update(Long id, SlideRequest request) {
        try {
            Slide slide = slideRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Slide not found"));
            
            slide.setCaption(request.getCaption());
            slide.setSubText(request.getSubText());
            slide.setImageUrl(request.getImageUrl());
            slide.setSortOrder(request.getSortOrder());
            slide.setPending(false); // Keep visible
            
            Slide saved = slideRepository.save(slide);
            
            // Evict homepage cache after content update
            homepageService.evictHomepageCache();
            
            return ApiResponse.ok("Slide updated successfully").with("slide", saved);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Slide Service.update() !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error updating slide: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse delete(Long id) {
        try {
            slideRepository.deleteById(id);
            
            // Evict homepage cache after content update
            homepageService.evictHomepageCache();
            
            return ApiResponse.ok("Slide deleted successfully");
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Slide Service.delete() !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error deleting slide: " + e.getMessage());
        }
    }
}
