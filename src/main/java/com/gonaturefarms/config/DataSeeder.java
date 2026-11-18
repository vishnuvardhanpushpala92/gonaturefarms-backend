package com.gonaturefarms.config;

import com.gonaturefarms.entity.FooterLink;
import com.gonaturefarms.entity.User;
import com.gonaturefarms.repository.FooterLinkRepository;
import com.gonaturefarms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data seeder to create the default admin user on first run.
 *
 * This component runs after application startup and checks if an admin user
 * exists. If not, it creates one using the configured default credentials.
 *
 * Uses ApplicationRunner with @Order to ensure it runs after Hibernate
 * has initialized and created the tables.
 */
@Component
@Order(100) // Run after other startup tasks
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final FooterLinkRepository footerLinkRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-user:Vishnu}")
    private String defaultAdminUsername;

    @Value("${app.admin.default-pass:918252}")
    private String defaultAdminPassword;

    public DataSeeder(UserRepository userRepository, FooterLinkRepository footerLinkRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.footerLinkRepository = footerLinkRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== DataSeeder Starting ===");
        log.info("Default admin username from config: {}", defaultAdminUsername);
        log.info("Default admin password from config: {}", defaultAdminPassword != null ? "***" : "null");

        try {
            // Check if admin user already exists by username (unique field)
            log.info("Checking if admin user already exists...");
            User existingAdmin = userRepository.findByUsername(defaultAdminUsername).orElse(null);
            if (existingAdmin != null) {
                log.info("Admin user already exists with username: {}, skipping seed", existingAdmin.getUsername());
                return;
            }

            // Create default admin user
            log.info("Creating default admin user with username: {}", defaultAdminUsername);

            User admin = User.builder()
                    .name("Default Admin")
                    .username(defaultAdminUsername)
                    .phone("0000000000") // Placeholder phone number
                    .email("admin@gonaturefarms.com")
                    .passwordHash(passwordEncoder.encode(defaultAdminPassword))
                    .role(User.UserRole.admin)
                    .securityQuestion("What is your default password?")
                    .securityAnswer(defaultAdminPassword)
                    .whatsappOptOut(false)
                    .build();

            User savedAdmin = userRepository.save(admin);
            log.info("=== Default admin user created successfully ===");
            log.info("Admin ID: {}", savedAdmin.getId());
            log.info("Admin Username: {}", savedAdmin.getUsername());
            log.info("Admin Email: {}", savedAdmin.getEmail());
            log.warn("=== SECURITY WARNING: Please change the default admin password immediately after first login! ===");

            // Seed default customer care links
            seedCustomerCareLinks();

        } catch (Exception e) {
            log.error("=== FAILED to seed default admin user ===", e);
            log.error("Error message: {}", e.getMessage());
            // Don't fail the application startup, just log the error
        }

        log.info("=== DataSeeder Complete ===");
    }

    private void seedCustomerCareLinks() {
        try {
            if (footerLinkRepository.count() > 0) {
                log.info("Footer links already exist, skipping seed");
                return;
            }

            log.info("Seeding default customer care links...");
            String[] customerCareLinks = {
                "WhatsApp Support|https://wa.me/919182526xxx",
                "Contact Support|support",
                "Track My Order|/tracking",
                "Raise a Complaint|/support",
                "Track Support Ticket|/support/tickets",
                "FAQs / Help Center|/help",
                "Shipping Policy|/policies/shipping",
                "Return & Refund Policy|/policies/return",
                "Contact Us|/contact"
            };

            for (int i = 0; i < customerCareLinks.length; i++) {
                String[] parts = customerCareLinks[i].split("\\|");
                FooterLink link = FooterLink.builder()
                        .name(parts[0])
                        .url(parts[1])
                        .category(FooterLink.LinkCategory.CUSTOMER_CARE)
                        .sortOrder(i)
                        .enabled(true)
                        .openInNewTab(parts[1].startsWith("http"))
                        .build();
                footerLinkRepository.save(link);
            }
            log.info("Default customer care links seeded successfully");
        } catch (Exception e) {
            log.error("Failed to seed customer care links", e);
        }
    }
}
