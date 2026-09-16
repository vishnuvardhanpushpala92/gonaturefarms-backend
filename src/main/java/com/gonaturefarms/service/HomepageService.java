package com.gonaturefarms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gonaturefarms.dto.common.ApiResponse;
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

/**
 * Homepage data aggregation service
 * Batches all required homepage data in a single request to reduce API calls
 */
@Service
public class HomepageService {

    private final SiteSettingRepository siteSettingRepository;
    private final SlideRepository slideRepository;
    private final ScrollBlockRepository scrollBlockRepository;
    private final FaqRepository faqRepository;
    private final DeliveryZoneRepository deliveryZoneRepository;
    private final FooterLinkRepository footerLinkRepository;
    private final TestimonialRepository testimonialRepository;
    private final VideoRepository videoRepository;
    private final ProductRepository productRepository;

    public HomepageService(
            SiteSettingRepository siteSettingRepository,
            SlideRepository slideRepository,
            ScrollBlockRepository scrollBlockRepository,
            FaqRepository faqRepository,
            DeliveryZoneRepository deliveryZoneRepository,
            FooterLinkRepository footerLinkRepository,
            TestimonialRepository testimonialRepository,
            VideoRepository videoRepository,
            ProductRepository productRepository) {
        this.siteSettingRepository = siteSettingRepository;
        this.slideRepository = slideRepository;
        this.scrollBlockRepository = scrollBlockRepository;
        this.faqRepository = faqRepository;
        this.deliveryZoneRepository = deliveryZoneRepository;
        this.footerLinkRepository = footerLinkRepository;
        this.testimonialRepository = testimonialRepository;
        this.videoRepository = videoRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse getHomepageData() {
        try {
            // Critical data for first viewport (optimized - only what's needed)
            Map<String, String> settings = getPublicSettings();
            List<?> slides = slideRepository.findAllByOrderBySortOrderAscIdAsc();
            List<?> blocks = scrollBlockRepository.findAllByOrderBySortOrderAscIdAsc();

            // Secondary data - fetch only enabled videos and limited products
            List<?> videos = videoRepository.findByEnabledTrueOrderByPriority();
            // Limit products to first 12 for initial load (pagination for more)
            List<?> products = productRepository.findAll();
            List<?> limitedProducts = products.size() > 12 ? products.subList(0, 12) : products;

            // Footer data - fetch separately if needed
            List<?> footerLinks = footerLinkRepository.findAll();

            return ApiResponse.ok()
                    .with("settings", settings)
                    .with("slides", slides)
                    .with("blocks", blocks)
                    .with("videos", videos)
                    .with("products", limitedProducts)
                    .with("footerLinks", footerLinks);
        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR IN HomepageService.getHomepageData() !!!");
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
