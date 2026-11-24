package com.gonaturefarms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.entity.AdminAuditLog;
import com.gonaturefarms.entity.User;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.repository.*;
import com.gonaturefarms.security.CurrentUser;
import com.gonaturefarms.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Secure admin data-deletion facility.
 * Requires fresh admin password verification on the backend, explicit
 * category selection, per-category record counts, and writes an audit log.
 * Users, orders, products, settings and admin accounts are never deletable here.
 */
@RestController
@RequestMapping("/api/admin/data")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDataDeletionController {

    private static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("complaints", "Customer Complaints");
        CATEGORIES.put("testimonials", "Testimonials");
        CATEGORIES.put("videos", "Videos");
        CATEGORIES.put("faqs", "FAQs");
        CATEGORIES.put("footer_links", "Footer Links");
        CATEGORIES.put("slides", "Hero Slides");
        CATEGORIES.put("scroll_blocks", "Scrolling Blocks");
        CATEGORIES.put("reviews", "Product Reviews");
        CATEGORIES.put("coupons", "Coupons");
        CATEGORIES.put("social_links", "Social Media Links");
    }

    private final SupportTicketRepository supportTicketRepository;
    private final TestimonialRepository testimonialRepository;
    private final VideoRepository videoRepository;
    private final FaqRepository faqRepository;
    private final FooterLinkRepository footerLinkRepository;
    private final SlideRepository slideRepository;
    private final ScrollBlockRepository scrollBlockRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final SocialMediaLinkRepository socialMediaLinkRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public AdminDataDeletionController(SupportTicketRepository supportTicketRepository,
                                       TestimonialRepository testimonialRepository,
                                       VideoRepository videoRepository,
                                       FaqRepository faqRepository,
                                       FooterLinkRepository footerLinkRepository,
                                       SlideRepository slideRepository,
                                       ScrollBlockRepository scrollBlockRepository,
                                       ReviewRepository reviewRepository,
                                       CouponRepository couponRepository,
                                       SocialMediaLinkRepository socialMediaLinkRepository,
                                       UserRepository userRepository,
                                       AdminAuditLogRepository auditLogRepository,
                                       PasswordEncoder passwordEncoder,
                                       ObjectMapper objectMapper) {
        this.supportTicketRepository = supportTicketRepository;
        this.testimonialRepository = testimonialRepository;
        this.videoRepository = videoRepository;
        this.faqRepository = faqRepository;
        this.footerLinkRepository = footerLinkRepository;
        this.slideRepository = slideRepository;
        this.scrollBlockRepository = scrollBlockRepository;
        this.reviewRepository = reviewRepository;
        this.couponRepository = couponRepository;
        this.socialMediaLinkRepository = socialMediaLinkRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/categories")
    public ApiResponse categories() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", CATEGORIES);
        result.put("counts", counts());
        return ApiResponse.ok().with("data", result);
    }

    private Map<String, Long> counts() {
        Map<String, Long> c = new LinkedHashMap<>();
        c.put("complaints", supportTicketRepository.count());
        c.put("testimonials", testimonialRepository.count());
        c.put("videos", videoRepository.count());
        c.put("faqs", faqRepository.count());
        c.put("footer_links", footerLinkRepository.count());
        c.put("slides", slideRepository.count());
        c.put("scroll_blocks", scrollBlockRepository.count());
        c.put("reviews", reviewRepository.count());
        c.put("coupons", couponRepository.count());
        c.put("social_links", socialMediaLinkRepository.count());
        return c;
    }

    @PostMapping("/delete")
    @Transactional
    public ResponseEntity<ApiResponse> deleteSelected(@RequestBody Map<String, Object> body) {
        try {
            CurrentUser admin = SecurityUtils.requireCurrentUser();
            String password = body.get("password") == null ? "" : String.valueOf(body.get("password"));
            Object catsObj = body.get("categories");
            if (!(catsObj instanceof List<?> rawCats) || rawCats.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("Select at least one data category"));
            }
            List<String> categories = rawCats.stream().map(String::valueOf).distinct().toList();
            for (String cat : categories) {
                if (!CATEGORIES.containsKey(cat)) {
                    return ResponseEntity.badRequest().body(ApiResponse.fail("Invalid category: " + cat));
                }
            }

            // Fresh credential verification on the backend — never trust the client.
            User user = userRepository.findById(admin.id())
                    .orElseThrow(() -> new ApiException("Admin account not found"));
            if (password.isBlank() || !passwordEncoder.matches(password, user.getPasswordHash())) {
                return ResponseEntity.status(401).body(ApiResponse.fail("Incorrect admin password"));
            }

            Map<String, Long> deleted = new LinkedHashMap<>();
            for (String cat : categories) {
                long before = countFor(cat);
                deleteFor(cat);
                deleted.put(cat, before);
            }

            try {
                auditLogRepository.save(AdminAuditLog.builder()
                        .adminId(admin.id())
                        .adminName(admin.name())
                        .action("DATA_DELETION")
                        .details(objectMapper.writeValueAsString(Map.of(
                                "categories", categories, "deleted_counts", deleted)))
                        .build());
            } catch (Exception e) {
                // Audit failure must not roll back the deletion silently — log via response details.
                deleted.put("_audit_error", -1L);
            }

            return ResponseEntity.ok(ApiResponse.ok("Selected data deleted successfully")
                    .with("deleted", deleted));
        } catch (ApiException e) {
            return ResponseEntity.status(400).body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.fail("Deletion failed"));
        }
    }

    private long countFor(String cat) {
        return switch (cat) {
            case "complaints" -> supportTicketRepository.count();
            case "testimonials" -> testimonialRepository.count();
            case "videos" -> videoRepository.count();
            case "faqs" -> faqRepository.count();
            case "footer_links" -> footerLinkRepository.count();
            case "slides" -> slideRepository.count();
            case "scroll_blocks" -> scrollBlockRepository.count();
            case "reviews" -> reviewRepository.count();
            case "coupons" -> couponRepository.count();
            case "social_links" -> socialMediaLinkRepository.count();
            default -> 0L;
        };
    }

    private void deleteFor(String cat) {
        switch (cat) {
            case "complaints" -> supportTicketRepository.deleteAll();
            case "testimonials" -> testimonialRepository.deleteAll();
            case "videos" -> videoRepository.deleteAll();
            case "faqs" -> faqRepository.deleteAll();
            case "footer_links" -> footerLinkRepository.deleteAll();
            case "slides" -> slideRepository.deleteAll();
            case "scroll_blocks" -> scrollBlockRepository.deleteAll();
            case "reviews" -> reviewRepository.deleteAll();
            case "coupons" -> couponRepository.deleteAll();
            case "social_links" -> socialMediaLinkRepository.deleteAll();
            default -> throw new ApiException("Invalid category: " + cat);
        }
    }
}
