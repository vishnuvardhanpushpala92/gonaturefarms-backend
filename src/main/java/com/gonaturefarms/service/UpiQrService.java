package com.gonaturefarms.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.SiteSetting;
import com.gonaturefarms.repository.SiteSettingRepository;

/**
 * Admin-managed UPI QR code stored in the existing Cloudinary account.
 *
 * Reuses the same Cloudinary credentials/configuration as the Video section.
 * QR metadata lives in the site_settings table:
 *   upi_qr_image_url        – Cloudinary secure_url shown to customers
 *   upi_qr_image_public_id  – Cloudinary public_id (for deletion on replace/remove)
 *   upi_qr_image_active     – 'true'/'false'
 *   upi_qr_image_uploaded_at– audit timestamp
 */
@Service
public class UpiQrService {

    private static final String FOLDER = "gonaturefarms/payment/upi-qr";
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp");

    private final Cloudinary cloudinary;
    private final SiteSettingRepository siteSettingRepository;

    public UpiQrService(Cloudinary cloudinary, SiteSettingRepository siteSettingRepository) {
        this.cloudinary = cloudinary;
        this.siteSettingRepository = siteSettingRepository;
    }

    /** Current QR state for the admin panel. */
    @Transactional(readOnly = true)
    public ApiResponse current() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("image_url", setting("upi_qr_image_url"));
        result.put("public_id", setting("upi_qr_image_public_id"));
        result.put("active", "true".equalsIgnoreCase(setting("upi_qr_image_active")));
        result.put("uploaded_at", setting("upi_qr_image_uploaded_at"));
        return ApiResponse.ok().with("qr", result);
    }

    /**
     * Upload (or replace) the UPI QR image.
     * The old Cloudinary asset is destroyed only AFTER the new reference is saved.
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public ApiResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("No file uploaded");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_TYPES.contains(contentType)) {
            return ApiResponse.fail("Invalid file type. Only PNG, JPG, JPEG or WEBP images are allowed.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            return ApiResponse.fail("File too large. Maximum size is 5 MB.");
        }

        String oldPublicId = setting("upi_qr_image_public_id");

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", FOLDER,
                    "public_id", "upi-qr-" + System.currentTimeMillis(),
                    "overwrite", false,
                    "secure", true,
                    "resource_type", "image");
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String newUrl = (String) uploadResult.get("secure_url");
            String newPublicId = (String) uploadResult.get("public_id");

            if (newUrl != null && newUrl.startsWith("http://")) {
                newUrl = newUrl.replaceFirst("^http://", "https://");
            }
            if (newUrl == null || newUrl.isBlank() || newPublicId == null || newPublicId.isBlank()) {
                return ApiResponse.fail("Cloudinary upload did not return an image URL");
            }

            // Save the new reference FIRST — only then remove the old asset.
            Map<String, String> updates = new LinkedHashMap<>();
            updates.put("upi_qr_image_url", newUrl);
            updates.put("upi_qr_image_public_id", newPublicId);
            updates.put("upi_qr_image_active", "true");
            updates.put("upi_qr_image_uploaded_at",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            putSettings(updates);

            // Replacement succeeded and new reference is safely stored → delete old asset.
            if (oldPublicId != null && !oldPublicId.isBlank() && !oldPublicId.equals(newPublicId)) {
                try {
                    cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
                } catch (Exception e) {
                    System.err.println("Failed to delete old UPI QR from Cloudinary: " + e.getMessage());
                }
            }

            return ApiResponse.ok("UPI QR code uploaded successfully")
                    .with("url", newUrl)
                    .with("public_id", newPublicId);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to upload UPI QR code: " + e.getMessage());
        }
    }

    /** Deactivate and remove the current QR (DB reference cleared, Cloudinary asset destroyed). */
    @Transactional
    public ApiResponse remove() {
        String publicId = setting("upi_qr_image_public_id");

        Map<String, String> updates = new LinkedHashMap<>();
        updates.put("upi_qr_image_url", "");
        updates.put("upi_qr_image_public_id", "");
        updates.put("upi_qr_image_active", "false");
        updates.put("upi_qr_image_uploaded_at", "");
        putSettings(updates);

        if (publicId != null && !publicId.isBlank()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                System.err.println("Failed to delete UPI QR from Cloudinary: " + e.getMessage());
            }
        }
        return ApiResponse.ok("UPI QR code removed");
    }

    /** Public read: active QR image URL for checkout (empty when none/inactive). */
    @Transactional(readOnly = true)
    public String activeImageUrl() {
        if (!"true".equalsIgnoreCase(setting("upi_qr_image_active"))) {
            return "";
        }
        String url = setting("upi_qr_image_url");
        return url == null ? "" : url.trim();
    }

    private void putSettings(Map<String, String> updates) {
        for (Map.Entry<String, String> e : updates.entrySet()) {
            SiteSetting s = siteSettingRepository.findByKey(e.getKey())
                    .orElseGet(() -> {
                        SiteSetting n = new SiteSetting();
                        n.setKey(e.getKey());
                        return n;
                    });
            s.setValue(e.getValue() == null ? "" : e.getValue());
            s.setUpdatedAt(LocalDateTime.now());
            siteSettingRepository.save(s);
        }
    }

    private String setting(String key) {
        return siteSettingRepository.findByKey(key)
                .map(SiteSetting::getValue)
                .map(v -> v == null ? "" : v)
                .orElse("");
    }
}
