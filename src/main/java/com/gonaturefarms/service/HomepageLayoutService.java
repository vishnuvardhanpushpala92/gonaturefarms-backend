package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.homepage.HomepageLayoutRequest;
import com.gonaturefarms.entity.HomepageLayout;
import com.gonaturefarms.repository.HomepageLayoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HomepageLayoutService {

    private final HomepageLayoutRepository homepageLayoutRepository;

    public HomepageLayoutService(HomepageLayoutRepository homepageLayoutRepository) {
        this.homepageLayoutRepository = homepageLayoutRepository;
    }

    /**
     * Get the currently published layout for public view
     */
    @Transactional(readOnly = true)
    public ApiResponse getPublishedLayout() {
        try {
            Optional<HomepageLayout> layout = homepageLayoutRepository.findByIsPublishedTrueOrderByUpdatedAtDesc();
            if (layout.isPresent()) {
                return ApiResponse.ok().with("layout", layout.get());
            }
            // Return default layout if none published
            return ApiResponse.ok().with("layout", getDefaultLayout());
        } catch (Exception e) {
            System.err.println("Error getting published layout: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail("Error loading layout: " + e.getMessage());
        }
    }

    /**
     * Get the latest layout (published or draft) for admin view
     */
    @Transactional(readOnly = true)
    public ApiResponse getLatestLayout() {
        try {
            Optional<HomepageLayout> layout = homepageLayoutRepository.findFirstByOrderByUpdatedAtDesc();
            if (layout.isPresent()) {
                return ApiResponse.ok().with("layout", layout.get());
            }
            // Return default layout if none exists
            return ApiResponse.ok().with("layout", getDefaultLayout());
        } catch (Exception e) {
            System.err.println("Error getting latest layout: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail("Error loading layout: " + e.getMessage());
        }
    }

    /**
     * Save or update homepage layout
     */
    @Transactional
    public ApiResponse saveLayout(HomepageLayoutRequest request, String username) {
        try {
            HomepageLayout layout;

            // Check if there's an existing layout
            Optional<HomepageLayout> existingLayout = homepageLayoutRepository.findFirstByOrderByUpdatedAtDesc();

            if (existingLayout.isPresent()) {
                layout = existingLayout.get();
            } else {
                layout = HomepageLayout.builder().build();
            }

            // Update hero settings
            if (request.getHeroHeightDesktop() != null) {
                layout.setHeroHeightDesktop(request.getHeroHeightDesktop());
            }
            if (request.getHeroHeightTablet() != null) {
                layout.setHeroHeightTablet(request.getHeroHeightTablet());
            }
            if (request.getHeroHeightMobile() != null) {
                layout.setHeroHeightMobile(request.getHeroHeightMobile());
            }
            if (request.getHeroImageFit() != null) {
                layout.setHeroImageFit(request.getHeroImageFit());
            }
            if (request.getHeroImagePosition() != null) {
                layout.setHeroImagePosition(request.getHeroImagePosition());
            }
            if (request.getHeroAspectRatio() != null) {
                layout.setHeroAspectRatio(request.getHeroAspectRatio());
            }
            if (request.getHeroBackgroundColor() != null) {
                layout.setHeroBackgroundColor(request.getHeroBackgroundColor());
            }

            // Update product settings
            if (request.getProductCardStyle() != null) {
                layout.setProductCardStyle(request.getProductCardStyle());
            }
            if (request.getProductImageAspectRatio() != null) {
                layout.setProductImageAspectRatio(request.getProductImageAspectRatio());
            }
            if (request.getProductImageFit() != null) {
                layout.setProductImageFit(request.getProductImageFit());
            }
            if (request.getProductsPerRowDesktop() != null) {
                layout.setProductsPerRowDesktop(request.getProductsPerRowDesktop());
            }
            if (request.getProductsPerRowTablet() != null) {
                layout.setProductsPerRowTablet(request.getProductsPerRowTablet());
            }
            if (request.getProductsPerRowMobile() != null) {
                layout.setProductsPerRowMobile(request.getProductsPerRowMobile());
            }
            if (request.getProductCardSpacing() != null) {
                layout.setProductCardSpacing(request.getProductCardSpacing());
            }

            // Update layout settings
            if (request.getSectionSpacing() != null) {
                layout.setSectionSpacing(request.getSectionSpacing());
            }
            if (request.getSectionWidth() != null) {
                layout.setSectionWidth(request.getSectionWidth());
            }
            if (request.getShowHero() != null) {
                layout.setShowHero(request.getShowHero());
            }
            if (request.getShowProducts() != null) {
                layout.setShowProducts(request.getShowProducts());
            }
            if (request.getShowTestimonials() != null) {
                layout.setShowTestimonials(request.getShowTestimonials());
            }
            if (request.getShowVideos() != null) {
                layout.setShowVideos(request.getShowVideos());
            }
            if (request.getShowFeatures() != null) {
                layout.setShowFeatures(request.getShowFeatures());
            }
            if (request.getProductSectionOrder() != null) {
                layout.setProductSectionOrder(request.getProductSectionOrder());
            }

            // Handle publication
            if (request.getPublish() != null && request.getPublish()) {
                // Unpublish all other layouts
                List<HomepageLayout> allLayouts = homepageLayoutRepository.findAll();
                allLayouts.forEach(l -> l.setIsPublished(false));
                homepageLayoutRepository.saveAll(allLayouts);

                layout.setIsPublished(true);
                layout.setIsDraft(false);
            } else if (request.getSaveAsDraft() != null && request.getSaveAsDraft()) {
                layout.setIsPublished(false);
                layout.setIsDraft(true);
            }

            layout.setUpdatedAt(LocalDateTime.now());
            layout.setUpdatedBy(username);

            if (layout.getId() == null) {
                layout.setCreatedAt(LocalDateTime.now());
                layout.setCreatedBy(username);
            }

            HomepageLayout savedLayout = homepageLayoutRepository.save(layout);

            String message = request.getPublish() != null && request.getPublish()
                ? "Layout published successfully"
                : "Layout saved as draft";

            return ApiResponse.ok(message).with("layout", savedLayout);
        } catch (Exception e) {
            System.err.println("Error saving layout: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail("Error saving layout: " + e.getMessage());
        }
    }

    /**
     * Reset to default layout
     */
    @Transactional
    public ApiResponse resetToDefaults(String username) {
        try {
            HomepageLayout defaultLayout = getDefaultLayout();
            defaultLayout.setCreatedAt(LocalDateTime.now());
            defaultLayout.setCreatedBy(username);
            defaultLayout.setUpdatedAt(LocalDateTime.now());
            defaultLayout.setUpdatedBy(username);

            HomepageLayout savedLayout = homepageLayoutRepository.save(defaultLayout);

            return ApiResponse.ok("Layout reset to defaults").with("layout", savedLayout);
        } catch (Exception e) {
            System.err.println("Error resetting layout: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.fail("Error resetting layout: " + e.getMessage());
        }
    }

    /**
     * Get default layout configuration
     */
    private HomepageLayout getDefaultLayout() {
        return HomepageLayout.builder()
            .heroHeightDesktop(450)
            .heroHeightTablet(320)
            .heroHeightMobile(250)
            .heroImageFit("contain")
            .heroImagePosition("center")
            .heroAspectRatio("16/9")
            .heroBackgroundColor("#f5f5f0")
            .productCardStyle("standard")
            .productImageAspectRatio("1/1")
            .productImageFit("cover")
            .productsPerRowDesktop(4)
            .productsPerRowTablet(3)
            .productsPerRowMobile(2)
            .productCardSpacing(16)
            .sectionSpacing(48)
            .sectionWidth("full")
            .showHero(true)
            .showProducts(true)
            .showTestimonials(true)
            .showVideos(true)
            .showFeatures(true)
            .productSectionOrder(1)
            .isPublished(false)
            .isDraft(true)
            .build();
    }
}
