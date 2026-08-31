package com.gonaturefarms.controller;

import com.gonaturefarms.entity.Faq;
import com.gonaturefarms.entity.Product;
import com.gonaturefarms.entity.SiteContent;
import com.gonaturefarms.entity.Video;
import com.gonaturefarms.repository.FaqRepository;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.repository.SiteContentRepository;
import com.gonaturefarms.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/commit")
public class AdminCommitController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SiteContentRepository siteContentRepository;

    @Autowired
    private FaqRepository faqRepository;

    @Autowired
    private VideoRepository videoRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> commitChanges() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Commit pending products
            List<Product> pendingProducts = productRepository.findByPendingTrue();
            int productCount = 0;
            for (Product product : pendingProducts) {
                product.setPending(false);
                productRepository.save(product);
                productCount++;
            }

            // Commit pending site content
            List<SiteContent> pendingSiteContent = siteContentRepository.findByPendingTrue();
            int siteContentCount = 0;
            for (SiteContent content : pendingSiteContent) {
                content.setPending(false);
                siteContentRepository.save(content);
                siteContentCount++;
            }

            // Commit pending FAQs
            List<Faq> pendingFaqs = faqRepository.findByPendingTrue();
            int faqCount = 0;
            for (Faq faq : pendingFaqs) {
                faq.setPending(false);
                faqRepository.save(faq);
                faqCount++;
            }

            // Commit pending videos
            List<Video> pendingVideos = videoRepository.findByPendingTrue();
            int videoCount = 0;
            for (Video video : pendingVideos) {
                video.setPending(false);
                videoRepository.save(video);
                videoCount++;
            }

            response.put("success", true);
            response.put("message", "Changes committed successfully");
            response.put("details", Map.of(
                "products", productCount,
                "siteContent", siteContentCount,
                "faqs", faqCount,
                "videos", videoCount,
                "total", productCount + siteContentCount + faqCount + videoCount
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to commit changes: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/pending-count")
    public ResponseEntity<Map<String, Object>> getPendingCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int productCount = productRepository.findByPendingTrue().size();
            int siteContentCount = siteContentRepository.findByPendingTrue().size();
            int faqCount = faqRepository.findByPendingTrue().size();
            int videoCount = videoRepository.findByPendingTrue().size();
            int total = productCount + siteContentCount + faqCount + videoCount;

            response.put("success", true);
            response.put("count", total);
            response.put("details", Map.of(
                "products", productCount,
                "siteContent", siteContentCount,
                "faqs", faqCount,
                "videos", videoCount
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get pending count: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
