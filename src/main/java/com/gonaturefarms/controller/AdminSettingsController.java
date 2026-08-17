package com.gonaturefarms.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.SiteSettingService;

import jakarta.annotation.PostConstruct;

/** GET /api/admin/settings/public (public) and PUT /api/admin/settings (admin). */
@RestController
@RequestMapping("/api/admin")
public class AdminSettingsController {

    private static final Set<String> ALLOWED_KEYS = Set.of(
            "site_name", "tagline", "footer_text", "payment_instructions", "store_location",
            "qr_code", "hdr_bg", "hdr_text", "ftr_bg", "ftr_text",
            "banner_msgs", "free_delivery_above", "delivery_charge_below", "whatsapp_number", "screenshot_number",
            "trust_badges", "footer_desc", "footer_phone", "support_fields", "footer_bg_image",
            "logo", "favicon"
    );

    private final SiteSettingService siteSettingService;
    private Cloudinary cloudinary;

    // Inject Cloudinary credentials from application.properties
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    public AdminSettingsController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
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

    @GetMapping("/settings/public")
    public ApiResponse publicSettings() {
        return siteSettingService.publicSettings();
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@RequestBody Map<String, String> body) {
        Map<String, String> filtered = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, String> e : body.entrySet()) {
            if (ALLOWED_KEYS.contains(e.getKey())) {
                filtered.put(e.getKey(), e.getValue());
            }
        }
        return siteSettingService.update(filtered).withMessage("Settings saved");
    }

    @PostMapping("/settings/footer-bg-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse uploadFooterBgImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail("File is empty");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "image"));
            String secureUrl = (String) uploadResult.get("secure_url");
            
            Map<String, String> updates = Map.of("footer_bg_image", secureUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("Footer background image uploaded").with("url", secureUrl);
        } catch (IOException e) {
            return ApiResponse.fail("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/settings/qr-code")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse uploadQrCode(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail("File is empty");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "image"));
            String secureUrl = (String) uploadResult.get("secure_url");
            
            Map<String, String> updates = Map.of("qr_code", secureUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("QR code uploaded").with("url", secureUrl);
        } catch (IOException e) {
            return ApiResponse.fail("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/settings/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse uploadLogo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail("File is empty");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "image"));
            String secureUrl = (String) uploadResult.get("secure_url");
            
            Map<String, String> updates = Map.of("logo", secureUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("Logo uploaded").with("url", secureUrl);
        } catch (IOException e) {
            return ApiResponse.fail("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/settings/favicon")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse uploadFavicon(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail("File is empty");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "image"));
            String secureUrl = (String) uploadResult.get("secure_url");
            
            Map<String, String> updates = Map.of("favicon", secureUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("Favicon uploaded").with("url", secureUrl);
        } catch (IOException e) {
            return ApiResponse.fail("Failed to upload file: " + e.getMessage());
        }
    }
}
