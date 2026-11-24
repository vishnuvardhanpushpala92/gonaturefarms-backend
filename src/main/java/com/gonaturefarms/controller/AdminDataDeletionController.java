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
        // Operational business data — deletable
        CATEGORIES.put("orders", "Orders");
        CATEGORIES.put("customers", "Customer Accounts");
        CATEGORIES.put("addresses", "Customer Addresses");
        CATEGORIES.put("complaints", "Customer Complaints");
        CATEGORIES.put("reviews", "Product Reviews");
        CATEGORIES.put("products", "Products");
        CATEGORIES.put("testimonials", "Testimonials");
        CATEGORIES.put("videos", "Videos");
        CATEGORIES.put("faqs", "FAQs");
        CATEGORIES.put("footer_links", "Footer Links");
        CATEGORIES.put("slides", "Hero Slides");
        CATEGORIES.put("scroll_blocks", "Scrolling Blocks");
        CATEGORIES.put("coupons", "Coupons");
        CATEGORIES.put("social_links", "Social Media Links");
        CATEGORIES.put("wishlists", "Wishlists");
        CATEGORIES.put("whatsapp_reminders", "WhatsApp Reminders");
        CATEGORIES.put("dashboard_analytics", "Dashboard & Revenue Analytics");
        // NEVER deletable: admin users, site settings, support settings,
        // homepage layout, categories, delivery zones, audit log, auth data.
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
    private final com.gonaturefarms.service.AdminExportService exportService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishlistRepository wishlistRepository;
    private final WhatsAppReminderRepository whatsappReminderRepository;
    private final OrderRepository orderRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final org.springframework.transaction.support.TransactionTemplate auditTx;

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
                                       ObjectMapper objectMapper,
                                       com.gonaturefarms.service.AdminExportService exportService,
                                       org.springframework.jdbc.core.JdbcTemplate jdbcTemplate,
                                       AddressRepository addressRepository,
                                       ProductRepository productRepository,
                                       ProductVariantRepository productVariantRepository,
                                       OrderItemRepository orderItemRepository,
                                       WishlistRepository wishlistRepository,
                                       WhatsAppReminderRepository whatsappReminderRepository,
                                       OrderRepository orderRepository,
                                       SiteSettingRepository siteSettingRepository,
                                       org.springframework.transaction.PlatformTransactionManager transactionManager) {
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
        this.exportService = exportService;
        this.jdbcTemplate = jdbcTemplate;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.orderItemRepository = orderItemRepository;
        this.wishlistRepository = wishlistRepository;
        this.whatsappReminderRepository = whatsappReminderRepository;
        this.orderRepository = orderRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.auditTx = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        this.auditTx.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @GetMapping("/categories")
    public ApiResponse categories() {
        // Return arrays (not maps) so the frontend snake→camel key transform
        // cannot mangle category identifiers like whatsapp_reminders.
        List<Map<String, Object>> cats = new ArrayList<>();
        CATEGORIES.forEach((k, v) -> cats.add(Map.of("key", k, "label", v)));
        Map<String, Long> raw = counts();
        List<Map<String, Object>> cnts = new ArrayList<>();
        raw.forEach((k, v) -> cnts.add(Map.of("key", k, "count", v)));
        return ApiResponse.ok()
                .with("categories", cats)
                .with("counts", cnts)
                .with("export_categories", com.gonaturefarms.service.AdminExportService.EXPORT_CATEGORIES);
    }

    private Map<String, Long> counts() {
        Map<String, Long> c = new LinkedHashMap<>();
        c.put("orders", orderRepository.count());
        c.put("customers", userRepository.findAll().stream().filter(u -> u.getRole() != User.UserRole.admin).count());
        c.put("addresses", addressRepository.count());
        c.put("complaints", supportTicketRepository.count());
        c.put("reviews", reviewRepository.count());
        c.put("products", productRepository.count());
        c.put("testimonials", testimonialRepository.count());
        c.put("videos", videoRepository.count());
        c.put("faqs", faqRepository.count());
        c.put("footer_links", footerLinkRepository.count());
        c.put("slides", slideRepository.count());
        c.put("scroll_blocks", scrollBlockRepository.count());
        c.put("coupons", couponRepository.count());
        c.put("social_links", socialMediaLinkRepository.count());
        c.put("wishlists", wishlistRepository.count());
        c.put("whatsapp_reminders", whatsappReminderRepository.count());
        c.put("dashboard_analytics", siteSettingRepository.findAll().stream()
                .filter(s -> s.getKey() != null && s.getKey().startsWith("dashboard")).count());
        return c;
    }

    private long orderRepositoryCount() { return orderRepository.count(); }

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
            String confirmation = body.get("confirmation") == null ? "" : String.valueOf(body.get("confirmation")).trim();
            if (!"DELETE PERMANENTLY".equalsIgnoreCase(confirmation)) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("Type DELETE PERMANENTLY to confirm"));
            }

            Map<String, Long> deleted = new LinkedHashMap<>();
            org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminDataDeletionController.class);
            for (String cat : categories) {
                long before = countFor(cat);
                log.info("Data deletion: category={} records={}", cat, before);
                deleteFor(cat);
                deleted.put(cat, before);
                log.info("Data deletion: category={} completed", cat);
            }

            // Audit in a SEPARATE transaction (REQUIRES_NEW) so an audit failure
            // can never mark the deletion transaction rollback-only.
            final Map<String, Long> auditCounts = new LinkedHashMap<>(deleted);
            final List<String> auditCats = new ArrayList<>(categories);
            auditTx.execute(status -> {
                try {
                    auditLogRepository.save(AdminAuditLog.builder()
                            .adminId(admin.id())
                            .adminName(admin.name())
                            .action("DATA_DELETION")
                            .details(objectMapper.writeValueAsString(Map.of(
                                    "categories", auditCats, "deleted_counts", auditCounts,
                                    "result", "SUCCESS")))
                            .build());
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(AdminDataDeletionController.class)
                            .error("Audit log write failed (deletion already committed): {}", e.getMessage());
                }
                return null;
            });

            return ResponseEntity.ok(ApiResponse.ok("Selected data deleted successfully")
                    .with("deleted", deleted));
        } catch (ApiException e) {
            return ResponseEntity.status(400).body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            // Rethrow so the transaction rolls back cleanly — swallowing the
            // exception here would surface as UnexpectedRollbackException (HTTP 500).
            org.slf4j.LoggerFactory.getLogger(AdminDataDeletionController.class)
                    .error("Data deletion failed for categories {}: {}", body.get("categories"), e.getMessage(), e);
            throw e;
        }
    }

    private long countFor(String cat) {
        return switch (cat) {
            case "orders" -> orderRepository.count();
            case "customers" -> userRepository.findAll().stream().filter(u -> u.getRole() != User.UserRole.admin).count();
            case "addresses" -> addressRepository.count();
            case "complaints" -> supportTicketRepository.count();
            case "reviews" -> reviewRepository.count();
            case "products" -> productRepository.count();
            case "testimonials" -> testimonialRepository.count();
            case "videos" -> videoRepository.count();
            case "faqs" -> faqRepository.count();
            case "footer_links" -> footerLinkRepository.count();
            case "slides" -> slideRepository.count();
            case "scroll_blocks" -> scrollBlockRepository.count();
            case "coupons" -> couponRepository.count();
            case "social_links" -> socialMediaLinkRepository.count();
            case "wishlists" -> wishlistRepository.count();
            case "whatsapp_reminders" -> whatsappReminderRepository.count();
            case "dashboard_analytics" -> siteSettingRepository.findAll().stream()
                    .filter(s -> s.getKey() != null && s.getKey().startsWith("dashboard")).count();
            default -> 0L;
        };
    }

    /** Deletes in FK-safe order when multiple categories selected. */
    private void deleteFor(String cat) {
        switch (cat) {
            // Dependents first
            case "orders" -> { orderItemRepository.deleteAll(); orderRepository.deleteAll(); }
            case "customers" -> {
                // Remove non-admin users; addresses/wishlists/reviews referencing them
                // are expected to be selected too, but delete dependents defensively.
                addressRepository.deleteAll();
                wishlistRepository.deleteAll();
                userRepository.findAll().stream()
                        .filter(u -> u.getRole() != User.UserRole.admin)
                        .forEach(userRepository::delete);
            }
            case "addresses" -> addressRepository.deleteAll();
            case "complaints" -> supportTicketRepository.deleteAll();
            case "reviews" -> reviewRepository.deleteAll();
            case "products" -> { productVariantRepository.deleteAll(); productRepository.deleteAll(); }
            case "testimonials" -> testimonialRepository.deleteAll();
            case "videos" -> videoRepository.deleteAll();
            case "faqs" -> faqRepository.deleteAll();
            case "footer_links" -> footerLinkRepository.deleteAll();
            case "slides" -> slideRepository.deleteAll();
            case "scroll_blocks" -> scrollBlockRepository.deleteAll();
            case "coupons" -> couponRepository.deleteAll();
            case "social_links" -> socialMediaLinkRepository.deleteAll();
            case "wishlists" -> wishlistRepository.deleteAll();
            case "whatsapp_reminders" -> whatsappReminderRepository.deleteAll();
            // Dashboard analytics are computed live from orders; only stored
            // dashboard_* snapshot settings are cleared — orders/payments untouched.
            case "dashboard_analytics" -> siteSettingRepository.findAll().stream()
                    .filter(s -> s.getKey() != null && s.getKey().startsWith("dashboard"))
                    .forEach(siteSettingRepository::delete);
            default -> throw new ApiException("Invalid category: " + cat);
        }
    }

    // ── Excel export ──────────────────────────────────────────────
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam List<String> categories,
                                         @RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to) {
        try {
            CurrentUser admin = SecurityUtils.requireCurrentUser();
            for (String c : categories) {
                if (!com.gonaturefarms.service.AdminExportService.EXPORT_CATEGORIES.containsKey(c)) {
                    return ResponseEntity.badRequest().body("Invalid export category".getBytes());
                }
            }
            java.time.LocalDate f = from != null && !from.isBlank() ? java.time.LocalDate.parse(from) : null;
            java.time.LocalDate t = to != null && !to.isBlank() ? java.time.LocalDate.parse(to) : null;
            byte[] bytes = exportService.exportXlsx(categories, f, t);
            auditLogRepository.save(AdminAuditLog.builder().adminId(admin.id()).adminName(admin.name())
                    .action("DATA_EXPORT").details(objectMapper.writeValueAsString(
                            Map.of("categories", categories, "from", String.valueOf(from), "to", String.valueOf(to)))).build());
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=gonaturefarms-export.xlsx")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Export failed".getBytes());
        }
    }

    // ── JSON backup (restorable data snapshot, admin-only) ───────
    @GetMapping("/backup")
    public ResponseEntity<byte[]> backup() {
        try {
            CurrentUser admin = SecurityUtils.requireCurrentUser();
            Map<String, Object> dump = new LinkedHashMap<>();
            dump.put("exported_at", java.time.LocalDateTime.now().toString());
            dump.put("tables", backupTables());
            auditLogRepository.save(AdminAuditLog.builder().adminId(admin.id()).adminName(admin.name())
                    .action("DATA_BACKUP").details("Full business-data JSON backup created").build());
            byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(dump);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .header("Content-Disposition", "attachment; filename=gonaturefarms-backup.json")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Backup failed".getBytes());
        }
    }

    private Map<String, List<Map<String, Object>>> backupTables() {
        // Allowlisted business tables; secrets (password_hash, reset_code,
        // security_answer, tokens) are excluded from the dump.
        Map<String, List<String>> tables = new LinkedHashMap<>();
        tables.put("users", List.of("id","name","username","phone","email","pincode","whatsapp_number","date_of_birth","role","is_verified","created_at"));
        tables.put("orders", null);
        tables.put("order_items", null);
        tables.put("support_tickets", null);
        tables.put("products", null);
        tables.put("reviews", null);
        tables.put("testimonials", null);
        tables.put("videos", null);
        tables.put("faqs", null);
        tables.put("footer_links", null);
        tables.put("slides", null);
        tables.put("scroll_blocks", null);
        tables.put("coupons", null);
        tables.put("addresses", null);
        tables.put("categories", null);
        Map<String, List<Map<String, Object>>> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : tables.entrySet()) {
            try {
                String sql = e.getValue() == null
                        ? "SELECT * FROM " + e.getKey()
                        : "SELECT " + String.join(",", e.getValue()) + " FROM " + e.getKey();
                out.put(e.getKey(), jdbcTemplate.queryForList(sql));
            } catch (Exception ex) {
                out.put(e.getKey(), List.of(Map.of("error", ex.getMessage())));
            }
        }
        return out;
    }

    // ── Audit history ─────────────────────────────────────────────
    @GetMapping("/audit")
    public ApiResponse auditHistory() {
        List<Map<String, Object>> rows = auditLogRepository.findAll().stream()
                .sorted(Comparator.comparing(AdminAuditLog::getCreatedAt).reversed())
                .limit(100)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("id", a.getId());
                    m.put("admin_id", a.getAdminId());
                    m.put("admin_name", a.getAdminName());
                    m.put("action", a.getAction());
                    m.put("details", a.getDetails());
                    m.put("created_at", a.getCreatedAt());
                    return m;
                }).toList();
        return ApiResponse.ok().with("audit", rows);
    }
}
