package com.gonaturefarms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Homepage data aggregation service
 * Batches all required homepage data in a single request to reduce API calls
 * Implements caching for public homepage data to reduce database load
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
    private final Executor taskExecutor;

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
            HomepageLayoutRepository homepageLayoutRepository,
            Executor taskExecutor) {
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
        this.taskExecutor = taskExecutor;
    }

    @Cacheable(value = "homepageData", key = "'public'")
    @Transactional(readOnly = true)
    public ApiResponse getHomepageData() {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();
        
        log.info("=== HOMEPAGE REQUEST START [{}] ===", requestId);
        
        try {
            // Critical data for first viewport
            Map<String, String> settings = getPublicSettings();
            List<?> slides = slideRepository.findAllByOrderBySortOrderAscIdAsc();
            List<?> blocks = scrollBlockRepository.findAllByOrderBySortOrderAscIdAsc();

            // Secondary data - fetch with limits for performance
            List<?> faqs = faqRepository.findTop6ByOrderBySortOrderAscIdAsc();
            List<?> zones = deliveryZoneRepository.findAll();
            List<?> footerLinks = footerLinkRepository.findTop8ByOrderBySortOrderAscIdAsc();
            List<?> testimonials = testimonialRepository.findTop4ByOrderByCreatedAtDesc();
            List<?> videos = videoRepository.findTop3ByEnabledTrueOrderByPriority();
            List<?> products = productRepository.findTop12ByOrderByCreatedAtDesc();
            Optional<HomepageLayout> layoutOpt = homepageLayoutRepository.findByIsPublishedTrueOrderByUpdatedAtDesc();
            HomepageLayout layout = layoutOpt.orElse(null);

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            log.info("=== HOMEPAGE REQUEST COMPLETE [{}] - TOTAL: {}ms ===", requestId, totalTime);
            
            return ApiResponse.ok()
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
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("!!! CRITICAL ERROR IN HomepageService.getHomepageData() [{}] - TOTAL: {}ms !!!", requestId, (endTime - startTime), e);
            e.printStackTrace();
            return ApiResponse.fail("Error loading homepage data: " + e.getMessage());
        }
    }

    /**
     * Invalidate homepage cache when admin publishes changes
     * Called by admin endpoints after content updates
     */
    @CacheEvict(value = "homepageData", key = "'public'")
    public void evictHomepageCache() {
        log.info("Homepage cache evicted - next request will fetch fresh data");
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
