package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * Stores uploaded images on disk, equivalent to the multer diskStorage configuration
 * in routes/admin.js. Files are written under app.upload.dir (default ./uploads) and
 * served back via the "/uploads/**" resource handler configured in WebConfig.
 */
@Service
public class FileStorageService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public ApiResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("No file uploaded");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException("Only image files allowed");
        }

        try {
            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);

            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String filename = System.currentTimeMillis() + "_" + randomSuffix(6) + ext;

            Path target = dir.resolve(filename);
            file.transferTo(target);

            return ApiResponse.ok("File uploaded").with("url", "/uploads/" + filename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
