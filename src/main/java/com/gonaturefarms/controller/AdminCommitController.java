package com.gonaturefarms.controller;

import com.gonaturefarms.entity.Category;
import com.gonaturefarms.entity.DeliveryZone;
import com.gonaturefarms.entity.Faq;
import com.gonaturefarms.entity.FooterLink;
import com.gonaturefarms.entity.Product;
import com.gonaturefarms.entity.ScrollBlock;
import com.gonaturefarms.entity.SiteContent;
import com.gonaturefarms.entity.Slide;
import com.gonaturefarms.entity.Video;
import com.gonaturefarms.repository.CategoryRepository;
import com.gonaturefarms.repository.DeliveryZoneRepository;
import com.gonaturefarms.repository.FaqRepository;
import com.gonaturefarms.repository.FooterLinkRepository;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.repository.ScrollBlockRepository;
import com.gonaturefarms.repository.SiteContentRepository;
import com.gonaturefarms.repository.SlideRepository;
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

    @Autowired
    private FooterLinkRepository footerLinkRepository;

    @Autowired
    private ScrollBlockRepository scrollBlockRepository;

    @Autowired
    private DeliveryZoneRepository deliveryZoneRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SlideRepository slideRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> commitChanges() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Commit pending products (including NULL as pending)
            List<Product> pendingProducts = productRepository.findByPendingTrue();
            int productCount = 0;
            for (Product product : pendingProducts) {
                product.setPending(false);
                productRepository.save(product);
                productCount++;
            }

            // Commit pending site content (including NULL as pending)
            List<SiteContent> pendingSiteContent = siteContentRepository.findByPendingTrue();
            int siteContentCount = 0;
            for (SiteContent content : pendingSiteContent) {
                content.setPending(false);
                siteContentRepository.save(content);
                siteContentCount++;
            }

            // Commit pending FAQs (including NULL as pending)
            List<Faq> pendingFaqs = faqRepository.findByPendingTrue();
            int faqCount = 0;
            for (Faq faq : pendingFaqs) {
                faq.setPending(false);
                faqRepository.save(faq);
                faqCount++;
            }

            // Commit pending videos (including NULL as pending)
            List<Video> pendingVideos = videoRepository.findByPendingTrue();
            int videoCount = 0;
            for (Video video : pendingVideos) {
                video.setPending(false);
                videoRepository.save(video);
                videoCount++;
            }

            // Commit pending footer links (including NULL as pending)
            List<FooterLink> pendingFooterLinks = footerLinkRepository.findByPendingTrue();
            int footerLinkCount = 0;
            for (FooterLink footerLink : pendingFooterLinks) {
                footerLink.setPending(false);
                footerLinkRepository.save(footerLink);
                footerLinkCount++;
            }

            // Commit pending scroll blocks (including NULL as pending)
            List<ScrollBlock> pendingScrollBlocks = scrollBlockRepository.findByPendingTrue();
            int scrollBlockCount = 0;
            for (ScrollBlock scrollBlock : pendingScrollBlocks) {
                scrollBlock.setPending(false);
                scrollBlockRepository.save(scrollBlock);
                scrollBlockCount++;
            }

            // Commit pending delivery zones (including NULL as pending)
            List<DeliveryZone> pendingDeliveryZones = deliveryZoneRepository.findByPendingTrue();
            int deliveryZoneCount = 0;
            for (DeliveryZone deliveryZone : pendingDeliveryZones) {
                deliveryZone.setPending(false);
                deliveryZoneRepository.save(deliveryZone);
                deliveryZoneCount++;
            }

            // Commit pending categories (including NULL as pending)
            List<Category> pendingCategories = categoryRepository.findByPendingTrue();
            int categoryCount = 0;
            for (Category category : pendingCategories) {
                category.setPending(false);
                categoryRepository.save(category);
                categoryCount++;
            }

            // Commit pending slides (including NULL as pending)
            List<Slide> pendingSlides = slideRepository.findByPendingTrue();
            int slideCount = 0;
            for (Slide slide : pendingSlides) {
                slide.setPending(false);
                slideRepository.save(slide);
                slideCount++;
            }

            response.put("success", true);
            response.put("message", "Changes committed successfully");
            response.put("details", Map.of(
                "products", productCount,
                "siteContent", siteContentCount,
                "faqs", faqCount,
                "videos", videoCount,
                "footerLinks", footerLinkCount,
                "scrollBlocks", scrollBlockCount,
                "deliveryZones", deliveryZoneCount,
                "categories", categoryCount,
                "slides", slideCount,
                "total", productCount + siteContentCount + faqCount + videoCount + footerLinkCount + scrollBlockCount + deliveryZoneCount + categoryCount + slideCount
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
            int footerLinkCount = footerLinkRepository.findByPendingTrue().size();
            int scrollBlockCount = scrollBlockRepository.findByPendingTrue().size();
            int deliveryZoneCount = deliveryZoneRepository.findByPendingTrue().size();
            int categoryCount = categoryRepository.findByPendingTrue().size();
            int slideCount = slideRepository.findByPendingTrue().size();
            int total = productCount + siteContentCount + faqCount + videoCount + footerLinkCount + scrollBlockCount + deliveryZoneCount + categoryCount + slideCount;

            response.put("success", true);
            response.put("count", total);
            response.put("details", Map.of(
                "products", productCount,
                "siteContent", siteContentCount,
                "faqs", faqCount,
                "videos", videoCount,
                "footerLinks", footerLinkCount,
                "scrollBlocks", scrollBlockCount,
                "deliveryZones", deliveryZoneCount,
                "categories", categoryCount,
                "slides", slideCount
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get pending count: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
