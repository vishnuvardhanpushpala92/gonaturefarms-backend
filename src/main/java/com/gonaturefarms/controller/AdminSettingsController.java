package com.gonaturefarms.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.gonaturefarms.repository.SiteSettingRepository;
import com.gonaturefarms.service.SiteSettingService;

/** GET /api/admin/settings/public (public) and PUT /api/admin/settings (admin). */
@RestController
@RequestMapping("/api/admin")
public class AdminSettingsController {

    private static final Set<String> ALLOWED_KEYS = Set.of(
            "site_name", "tagline", "footer_text", "payment_instructions", "store_location",
            "upi_scanner_url", "upi_scanner_public_id", "hdr_bg", "hdr_text", "hdr_font_size", 
            "ftr_bg", "ftr_text", "ftr_font_size",
            "banner_msgs", "free_delivery_above", "delivery_charge_below", "whatsapp_number", "screenshot_number",
            "trust_badges", "footer_desc", "footer_phone", "support_fields", "footer_bg_image",
            "logo", "favicon", "upi_id"
    );

    private final SiteSettingService siteSettingService;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private SiteSettingRepository siteSettingRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

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
            String uploadDirPath = uploadDir + "/footer";
            File directory = new File(uploadDirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDirPath, filename);
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/footer/" + filename;
            
            Map<String, String> updates = Map.of("footer_bg_image", fileUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("Footer background image uploaded").with("url", fileUrl);
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
            String uploadDirPath = uploadDir + "/qr";
            File directory = new File(uploadDirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDirPath, filename);
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/qr/" + filename;
            
            Map<String, String> updates = Map.of("qr_code", fileUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("QR code uploaded").with("url", fileUrl);
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
            String uploadDirPath = uploadDir + "/logo";
            File directory = new File(uploadDirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDirPath, filename);
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/logo/" + filename;
            
            Map<String, String> updates = Map.of("logo", fileUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("Logo uploaded").with("url", fileUrl);
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
            String uploadDirPath = uploadDir + "/favicon";
            File directory = new File(uploadDirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDirPath, filename);
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/favicon/" + filename;
            
            Map<String, String> updates = Map.of("favicon", fileUrl);
            siteSettingService.update(updates);
            
            return ApiResponse.ok("Favicon uploaded").with("url", fileUrl);
        } catch (IOException e) {
            return ApiResponse.fail("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/settings/upi-scanner")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse uploadUpiScanner(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail("File is empty");
        }

        try {
            // Check if there's an existing UPI scanner image and delete it from Cloudinary
            var existingPublicId = siteSettingRepository.findByKey("upi_scanner_public_id")
                    .map(com.gonaturefarms.entity.SiteSetting::getValue)
                    .orElse(null);

            if (existingPublicId != null && !existingPublicId.isEmpty()) {
                try {
                    cloudinary.uploader().destroy(existingPublicId, ObjectUtils.emptyMap());
                } catch (Exception e) {
                    // Log but continue with upload
                    System.err.println("Failed to delete old Cloudinary image: " + e.getMessage());
                }
            }

            // Upload new image to Cloudinary
            var uploadParams = ObjectUtils.asMap(
                    "folder", "settings",
                    "public_id", "upi_scanner_" + System.currentTimeMillis(),
                    "overwrite", true,
                    "secure", true,
                    "resource_type", "image"
            );

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String newUrl = (String) uploadResult.get("secure_url");
            String newPublicId = (String) uploadResult.get("public_id");

            // Ensure URL is HTTPS
            if (newUrl != null && newUrl.startsWith("http://")) {
                newUrl = newUrl.replace("http://", "https://");
            }

            // Save new URL and public ID to database
            Map<String, String> updates = new java.util.LinkedHashMap<>();
            updates.put("upi_scanner_url", newUrl);
            updates.put("upi_scanner_public_id", newPublicId);
            siteSettingService.update(updates);

            return ApiResponse.ok("UPI Scanner uploaded successfully").with("url", newUrl);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to upload UPI Scanner: " + e.getMessage());
        }
    }
}
