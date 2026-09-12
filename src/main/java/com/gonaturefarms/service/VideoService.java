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
import com.gonaturefarms.entity.Product;
import com.gonaturefarms.repository.VideoRepository;
import com.gonaturefarms.repository.ProductRepository;

import jakarta.annotation.PostConstruct;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final ProductRepository productRepository;
    private Cloudinary cloudinary;

    // Inject Cloudinary credentials from application.properties
    @Value("${cloudinary.cloud.name}")
    private String cloudName;

    @Value("${cloudinary.api.key}")
    private String apiKey;

    @Value("${cloudinary.api.secret}")
    private String apiSecret;

    public VideoService(VideoRepository videoRepository, ProductRepository productRepository) {
        this.videoRepository = videoRepository;
        this.productRepository = productRepository;
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
        try {
            List<Video> videos = videoRepository.findByEnabledTrueOrderByPriority();
            // Filter out pending videos for public view (treat NULL as false)
            List<Video> publicVideos = videos.stream()
                    .filter(v -> v.getPending() == null || !v.getPending())
                    .collect(Collectors.toList());

            // Enrich videos with product information
            List<Map<String, Object>> enrichedVideos = publicVideos.stream()
                    .map(this::enrichVideoWithProduct)
                    .collect(Collectors.toList());

            return ApiResponse.ok().with("videos", enrichedVideos);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Video Service.getAllEnabled() !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error loading videos: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse adminAll() {
        try {
            List<Video> videos = videoRepository.findAll();
            // Sort videos by priority (NULLs last)
            videos.sort((a, b) -> {
                if (a.getSortOrder() == null && b.getSortOrder() == null) {
                    return 0;
                } else if (a.getSortOrder() == null) {
                    return 1;
                } else if (b.getSortOrder() == null) {
                    return -1;
                } else {
                    return a.getSortOrder().compareTo(b.getSortOrder());
                }
            });
            // Enrich videos with product information
            List<Map<String, Object>> enrichedVideos = videos.stream()
                    .map(this::enrichVideoWithProduct)
                    .collect(Collectors.toList());
            return ApiResponse.ok().with("videos", enrichedVideos);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN Admin Videos Service !!!");
            e.printStackTrace();
            return ApiResponse.fail("Error loading admin videos: " + e.getMessage());
        }
    }

    private Map<String, Object> enrichVideoWithProduct(Video video) {
        Map<String, Object> enriched = new java.util.HashMap<>();
        enriched.put("id", video.getId());
        enriched.put("title", video.getTitle());
        enriched.put("filePath", video.getFilePath());
        enriched.put("posterUrl", video.getPosterUrl());
        enriched.put("productId", video.getProductId());
        enriched.put("enabled", video.getEnabled());
        enriched.put("sortOrder", video.getSortOrder());
        enriched.put("orientation", video.getOrientation());
        enriched.put("pending", video.getPending());
        enriched.put("createdAt", video.getCreatedAt());
        enriched.put("updatedAt", video.getUpdatedAt());
        
        // Fetch product information if productId is set
        if (video.getProductId() != null) {
            productRepository.findById(video.getProductId()).ifPresent(product -> {
                Map<String, Object> productInfo = new java.util.HashMap<>();
                productInfo.put("id", product.getId());
                productInfo.put("name", product.getName());
                productInfo.put("price", product.getPrice());
                productInfo.put("imgUrl", product.getImgUrl());
                enriched.put("product", productInfo);
            });
        }
        
        return enriched;
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
        return ApiResponse.ok("Video created successfully").with("video", enrichVideoWithProduct(saved));
    }

    @Transactional
    public ApiResponse update(Long id, Video video, MultipartFile file) {
        return videoRepository.findById(id)
            .map(existing -> {
                existing.setTitle(video.getTitle());
                existing.setProductId(video.getProductId());
                existing.setPosterUrl(video.getPosterUrl());
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
                return ApiResponse.ok("Video updated successfully").with("video", enrichVideoWithProduct(updated));
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

    @Transactional
    public ApiResponse approve(Long id) {
        return videoRepository.findById(id)
            .map(video -> {
                video.setPending(false);
                Video updated = videoRepository.save(video);
                return ApiResponse.ok("Video approved successfully").with("video", enrichVideoWithProduct(updated));
            })
            .orElse(ApiResponse.fail("Video not found"));
    }

    @Transactional
    public ApiResponse reject(Long id) {
        return videoRepository.findById(id)
            .map(video -> {
                videoRepository.deleteById(id);
                return ApiResponse.ok("Video rejected and deleted successfully");
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