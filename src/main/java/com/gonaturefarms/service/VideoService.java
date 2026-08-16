package com.gonaturefarms.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.Video;
import com.gonaturefarms.repository.VideoRepository;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public ApiResponse getAllEnabled() {
        List<Video> videos = videoRepository.findByEnabledTrueOrderBySortOrderAsc();
        return ApiResponse.ok().with("videos", videos);
    }

    // FIXED: Added Transactional and try-catch to prevent 500 crashes
    @Transactional(readOnly = true)
    public ApiResponse adminAll() {
        try {
            List<Video> videos = videoRepository.findAll();
            return ApiResponse.ok().with("videos", videos);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Admin Videos Service !!!");
            e.printStackTrace();
            // This ensures the frontend gets a clear message instead of crashing with 500
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
                
                if (file != null && !file.isEmpty()) {
                    if (existing.getFilePath() != null) {
                        deleteFile(existing.getFilePath());
                    }
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
                if (video.getFilePath() != null) {
                    deleteFile(video.getFilePath());
                }
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

    private String saveFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/videos";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());
            
            return "/uploads/videos/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save video file", e);
        }
    }

    private void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath.replaceFirst("^/", ""));
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + filePath);
        }
    }
}