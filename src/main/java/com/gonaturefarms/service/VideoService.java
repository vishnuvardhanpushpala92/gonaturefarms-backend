package com.gonaturefarms.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Video;
import com.gonaturefarms.repository.VideoRepository;

import jakarta.annotation.PostConstruct;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private Cloudinary cloudinary;

    // Inject Cloudinary credentials from application.properties
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
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

    public ApiResponse getAllEnabled() {
        List<Video> videos = videoRepository.findByEnabledTrueOrderBySortOrderAsc();
        // Filter out pending videos for public view
        List<Video> publicVideos = videos.stream()
                .filter(v -> !v.getPending())
                .collect(Collectors.toList());
        return ApiResponse.ok().with("videos", publicVideos);
    }

    @Transactional(readOnly = true)
    public ApiResponse adminAll() {
        try {
            List<Video> videos = videoRepository.findAll();
            return ApiResponse.ok().with("videos", videos);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Admin Videos Service !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error loading admin videos: " + e.getMessage());
        }
    }

    @Transactional
    public ApiResponse create(Video video, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("Video file is required");
        }

        String filePath = saveFile(file);
        video.setFilePath(filePath);
        video.setPending(true);

        Video saved = videoRepository.save(video);
        return ApiResponse.ok("Video created successfully").with("video", saved);
    }

    @Transactional
    public ApiResponse update(Long id, Video video, MultipartFile file) {
        return videoRepository.findById(id)
            .map(existing -> {
                existing.setTitle(video.getTitle());
                existing.setEnabled(video.getEnabled());
                existing.setSortOrder(video.getSortOrder());
                existing.setPending(true);

                if (file != null && !file.isEmpty()) {
                    // Note: We no longer delete the old local file because it's stored on Cloudinary.
                    // If you want to delete the old Cloudinary file, we would need to extract the public_id
                    // and call cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap()).
                    String filePath = saveFile(file);
                    existing.setFilePath(filePath);
                }

                Video updated = videoRepository.save(existing);
                return ApiResponse.ok("Video updated successfully").with("video", updated);
            })
            .orElse(ApiResponse.fail("Video not found"));
    }

    @Transactional
    public ApiResponse delete(Long id) {
        return videoRepository.findById(id)
            .map(video -> {
                // We don't need to delete a local file anymore. The file lives permanently on Cloudinary.
                // If you want to delete from Cloudinary as well, uncomment the line below and extract the public ID.
                // deleteFromCloudinary(video.getFilePath());
                videoRepository.deleteById(id);
                return ApiResponse.ok("Video deleted successfully");
            })
            .orElse(ApiResponse.fail("Video not found"));
    }

    @Transactional
    public ApiResponse toggleEnabled(Long id) {
        return videoRepository.findById(id)
            .map(video -> {
                video.setEnabled(!video.getEnabled());
                Video updated = videoRepository.save(video);
                return ApiResponse.ok("Video status updated").with("video", updated);
            })
            .orElse(ApiResponse.fail("Video not found"));
    }

    // NEW: This is the permanent fix. It uploads directly to Cloudinary and returns the URL.
    private String saveFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "video"));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload video to Cloudinary", e);
        }
    }

    // Removed local deleteFile() method because we no longer store videos on the server's hard drive.
}