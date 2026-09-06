package com.gonaturefarms.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Testimonial;
import com.gonaturefarms.repository.TestimonialRepository;

@Service
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    public TestimonialService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
    }

    public ApiResponse getAllEnabled() {
        List<Testimonial> testimonials = testimonialRepository.findByEnabledTrueOrderBySortOrderAsc();
        // Filter out pending testimonials for public view (treat NULL as false)
        List<Testimonial> publicTestimonials = testimonials.stream()
                .filter(t -> t.getPending() == null || !t.getPending())
                .collect(Collectors.toList());
        return ApiResponse.ok().with("testimonials", publicTestimonials);
    }

    @Transactional(readOnly = true)
    public ApiResponse adminAll() {
        try {
            List<Testimonial> testimonials = testimonialRepository.findAll();
            return ApiResponse.ok().with("testimonials", testimonials);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Admin Testimonials Service !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error loading admin testimonials: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse create(Testimonial testimonial) {
        testimonial.setPending(true);
        Testimonial saved = testimonialRepository.save(testimonial);
        return ApiResponse.ok("Testimonial created successfully").with("testimonial", saved);
    }

    @Transactional
    public ApiResponse update(Long id, Testimonial testimonial) {
        return testimonialRepository.findById(id)
            .map(existing -> {
                existing.setCustomerName(testimonial.getCustomerName());
                existing.setQuote(testimonial.getQuote());
                existing.setRating(testimonial.getRating());
                existing.setAvatarUrl(testimonial.getAvatarUrl());
                existing.setEnabled(testimonial.getEnabled());
                existing.setSortOrder(testimonial.getSortOrder());
                existing.setPending(true);
                Testimonial updated = testimonialRepository.save(existing);
                return ApiResponse.ok("Testimonial updated successfully").with("testimonial", updated);
            })
            .orElse(ApiResponse.fail("Testimonial not found"));
    }

    @Transactional
    public ApiResponse delete(Long id) {
        return testimonialRepository.findById(id)
            .map(testimonial -> {
                testimonialRepository.deleteById(id);
                return ApiResponse.ok("Testimonial deleted successfully");
            })
            .orElse(ApiResponse.fail("Testimonial not found"));
    }

    @Transactional
    public ApiResponse toggleEnabled(Long id) {
        return testimonialRepository.findById(id)
            .map(testimonial -> {
                testimonial.setEnabled(!testimonial.getEnabled());
                Testimonial updated = testimonialRepository.save(testimonial);
                return ApiResponse.ok("Testimonial status updated").with("testimonial", updated);
            })
            .orElse(ApiResponse.fail("Testimonial not found"));
    }
}