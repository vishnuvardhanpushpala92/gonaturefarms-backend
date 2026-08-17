package com.gonaturefarms.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gonaturefarms.dto.admin.SlideRequest;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Slide;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.SlideRepository;

import jakarta.annotation.PostConstruct;

@Service
public class SlideService {

    private final SlideRepository slideRepository;
    private Cloudinary cloudinary;

    // Inject Cloudinary credentials from application.properties
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    public SlideService(SlideRepository slideRepository) {
        this.slideRepository = slideRepository;
    }

    // Initialize Cloudinary once the Spring bean is created
    @PostConstruct
    public void init() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
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
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder().intValue() : 0)
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

    @Transactional
    public ApiResponse uploadSlideImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("No file uploaded");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException("Only image files allowed");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "image"));
            String secureUrl = (String) uploadResult.get("secure_url");
            return ApiResponse.ok("Image uploaded successfully").with("url", secureUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }
}
