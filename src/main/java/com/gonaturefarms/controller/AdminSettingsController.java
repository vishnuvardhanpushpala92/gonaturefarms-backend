package com.gonaturefarms.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.service.SiteSettingService;

/** GET /api/admin/settings/public (public) and PUT /api/admin/settings (admin). */
@RestController
@RequestMapping("/api/admin")
public class AdminSettingsController {

    private static final Set<String> ALLOWED_KEYS = Set.of(
            "site_name", "tagline", "footer_text", "payment_instructions", "store_location",
            "qr_code", "logo_url", "hdr_bg", "hdr_text", "ftr_bg", "ftr_text",
            "banner_msgs", "free_delivery_above", "delivery_charge_below", "whatsapp_number", "screenshot_number",
            "trust_badges", "footer_desc", "footer_phone", "support_fields", "footer_bg_image"
    );

    private final SiteSettingService siteSettingService;

    public AdminSettingsController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
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
            String uploadDir = "uploads/footer";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/footer/" + filename;
            
            Map<String, String> updates = Map.of("footer_bg_image", fileUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("Footer background image uploaded").with("url", fileUrl);
        } catch (IOException e) {
            return ApiResponse.fail("Failed to upload file: " + e.getMessage());
        }
    }
}
