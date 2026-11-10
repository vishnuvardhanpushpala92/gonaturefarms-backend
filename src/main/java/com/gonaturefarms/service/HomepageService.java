package com.gonaturefarms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.HomepageLayout;
import com.gonaturefarms.repository.HomepageLayoutRepository;
import com.gonaturefarms.repository.ScrollBlockRepository;
import com.gonaturefarms.repository.SlideRepository;
import com.gonaturefarms.repository.SiteSettingRepository;
import com.gonaturefarms.repository.FaqRepository;
import com.gonaturefarms.repository.DeliveryZoneRepository;
import com.gonaturefarms.repository.FooterLinkRepository;
import com.gonaturefarms.repository.TestimonialRepository;
import com.gonaturefarms.repository.VideoRepository;
import com.gonaturefarms.repository.ProductRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Homepage data aggregation service
 * Batches all required homepage data in a single request to reduce API calls
 */
@Service
public class HomepageService {

    private static final Logger log = LoggerFactory.getLogger(HomepageService.class);

    private final SiteSettingRepository siteSettingRepository;
    private final SlideRepository slideRepository;
    private final ScrollBlockRepository scrollBlockRepository;
    private final FaqRepository faqRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final FooterLinkRepository footerLinkRepository;
    private final TestimonialRepository testimonialRepository;
    private final VideoRepository videoRepository;
    private final ProductRepository productRepository;
    private final HomepageLayoutRepository homepageLayoutRepository;

    public HomepageService(
            SiteSettingRepository siteSettingRepository,
            SlideRepository slideRepository,
            ScrollBlockRepository scrollBlockRepository,
            FaqRepository faqRepository,
            DeliveryZoneRepository deliveryZoneRepository,
            FooterLinkRepository footerLinkRepository,
            TestimonialRepository testimonialRepository,
            VideoRepository videoRepository,
            ProductRepository productRepository,
            HomepageLayoutRepository homepageLayoutRepository) {
        this.siteSettingRepository = siteSettingRepository;
        this.slideRepository = slideRepository;
        this.scrollBlockRepository = scrollBlockRepository;
        this.faqRepository = faqRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.footerLinkRepository = footerLinkRepository;
        this.testimonialRepository = testimonialRepository;
        this.videoRepository = videoRepository;
        this.productRepository = productRepository;
        this.homepageLayoutRepository = homepageLayoutRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse getHomepageData() {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();
        
        log.info("=== HOMEPAGE REQUEST START [{}] ===", requestId);
        
        try {
            long t0 = System.currentTimeMillis();
            
            // Critical data for first viewport
            long t1 = System.currentTimeMillis();
            Map<String, String> settings = getPublicSettings();
            long t2 = System.currentTimeMillis();
            log.info("[{}] Settings: {} records ({}ms)", requestId, settings.size(), (t2 - t1));
            
            long t3 = System.currentTimeMillis();
            List<?> slides = slideRepository.findAllByOrderBySortOrderAscIdAsc();
            long t4 = System.currentTimeMillis();
            log.info("[{}] Slides: {} records ({}ms)", requestId, slides.size(), (t4 - t3));
            
            long t5 = System.currentTimeMillis();
            List<?> blocks = scrollBlockRepository.findAllByOrderBySortOrderAscIdAsc();
            long t6 = System.currentTimeMillis();
            log.info("[{}] Blocks: {} records ({}ms)", requestId, blocks.size(), (t6 - t5));

            // Secondary data - fetch with limits for performance
            long t7 = System.currentTimeMillis();
            List<?> faqs = faqRepository.findTop6ByOrderBySortOrderAscIdAsc();
            long t8 = System.currentTimeMillis();
            log.info("[{}] FAQs: {} records ({}ms)", requestId, faqs.size(), (t8 - t7));
            
            long t9 = System.currentTimeMillis();
            List<?> zones = deliveryZoneRepository.findAll();
            long t10 = System.currentTimeMillis();
            log.info("[{}] Zones: {} records ({}ms)", requestId, zones.size(), (t10 - t9));
            
            long t11 = System.currentTimeMillis();
            List<?> footerLinks = footerLinkRepository.findTop8ByOrderBySortOrderAscIdAsc();
            long t12 = System.currentTimeMillis();
            log.info("[{}] FooterLinks: {} records ({}ms)", requestId, footerLinks.size(), (t12 - t11));
            
            long t13 = System.currentTimeMillis();
            List<?> testimonials = testimonialRepository.findTop4ByOrderByCreatedAtDesc();
            long t14 = System.currentTimeMillis();
            log.info("[{}] Testimonials: {} records ({}ms)", requestId, testimonials.size(), (t14 - t13));
            
            long t15 = System.currentTimeMillis();
            List<?> videos = videoRepository.findTop3ByEnabledTrueOrderByPriority();
            long t16 = System.currentTimeMillis();
            log.info("[{}] Videos: {} records ({}ms)", requestId, videos.size(), (t16 - t15));
            
            // Limit products to latest ones for better performance
            long t17 = System.currentTimeMillis();
            List<?> products = productRepository.findTop12ByOrderByCreatedAtDesc();
            long t18 = System.currentTimeMillis();
            log.info("[{}] Products: {} records ({}ms)", requestId, products.size(), (t18 - t17));

            // Fetch published homepage layout configuration
            long t19 = System.currentTimeMillis();
            Optional<HomepageLayout> layoutOpt = homepageLayoutRepository.findByIsPublishedTrueOrderByUpdatedAtDesc();
            HomepageLayout layout = layoutOpt.orElse(null);
            long t20 = System.currentTimeMillis();
            log.info("[{}] Layout: {} ({}ms)", requestId, layout != null ? "published" : "none", (t20 - t19));

            long t21 = System.currentTimeMillis();
            ApiResponse response = ApiResponse.ok()
                    .with("settings", settings)
                    .with("slides", slides)
                    .with("blocks", blocks)
                    .with("faqs", faqs)
                    .with("zones", zones)
                    .with("footerLinks", footerLinks)
                    .with("testimonials", testimonials)
                    .with("videos", videos)
                    .with("products", products)
                    .with("layout", layout);
            
            long endTime = System.currentTimeMillis();
            log.info("=== HOMEPAGE REQUEST COMPLETE [{}] - TOTAL: {}ms ===", requestId, (endTime - startTime));
            
            return response;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("!!! CRITICAL ERROR IN HomepageService.getHomepageData() [{}] - TOTAL: {}ms !!!", requestId, (endTime - startTime), e);
            e.printStackTrace();
            return ApiResponse.fail("Error loading homepage data: " + e.getMessage());
        }
    }

    private Map<String, String> getPublicSettings() {
        List<?> all = siteSettingRepository.findAll();
        Map<String, String> map = new LinkedHashMap<>();
        for (Object s : all) {
            // Handle both SiteSetting entity and potential other types
            if (s instanceof com.gonaturefarms.entity.SiteSetting) {
                com.gonaturefarms.entity.SiteSetting setting = (com.gonaturefarms.entity.SiteSetting) s;
                map.put(setting.getKey(), setting.getValue());
            }
        }
        return map;
    }
}
