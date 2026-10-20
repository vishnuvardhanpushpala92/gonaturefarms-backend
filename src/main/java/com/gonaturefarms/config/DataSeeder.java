package com.gonaturefarms.config;

import com.gonaturefarms.entity.User;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-user:Vishnu}")
    private String defaultAdminUsername;

    @Value("${app.admin.default-pass:918252}")
    private String defaultAdminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== DataSeeder Starting ===");
        log.info("Default admin username from config: {}", defaultAdminUsername);
        log.info("Default admin password from config: {}", defaultAdminPassword != null ? "***" : "null");

        try {
            // Check if admin user already exists
            log.info("Checking if admin user already exists...");
            long adminCount = userRepository.countByRole(User.UserRole.admin);
            log.info("Admin user count: {}", adminCount);

            if (adminCount > 0) {
                log.info("Admin user already exists (count: {}), skipping seed", adminCount);
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

        } catch (Exception e) {
            log.error("=== FAILED to seed default admin user ===", e);
            log.error("Error message: {}", e.getMessage());
            // Don't fail the application startup, just log the error
        }

        log.info("=== DataSeeder Complete ===");
    }
}
