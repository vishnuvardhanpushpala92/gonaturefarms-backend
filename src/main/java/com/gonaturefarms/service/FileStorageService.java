package com.gonaturefarms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import jakarta.annotation.PostConstruct;

/**
 * Stores uploaded images to Cloudinary, replacing local disk storage.
 * Images are permanently stored on Cloudinary and the secure_url is returned.
 */
@Service
public class FileStorageService {

    private Cloudinary cloudinary;

    // Inject Cloudinary credentials from application.properties
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    // Initialize Cloudinary once the Spring bean is created
    @PostConstruct
    public void init() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
    }

    public ApiResponse store(MultipartFile file) {
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
            return ApiResponse.ok("File uploaded").with("url", secureUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }
}
