package com.gonaturefarms.config;

import com.gonaturefarms.entity.User;
import com.gonaturefarms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data seeder to create the default admin user on first run.
 * 
 * This component runs after application startup and checks if an admin user
 * exists. If not, it creates one using the configured default credentials.
 */
@Component
public class DataSeeder implements CommandLineRunner {

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
    public void run(String... args) {
        try {
            // Check if admin user already exists
            boolean adminExists = userRepository.existsByRole(User.UserRole.admin);
            
            if (adminExists) {
                log.info("Admin user already exists, skipping seed");
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

            userRepository.save(admin);
            log.info("Default admin user created successfully with ID: {}", admin.getId());
            log.warn("SECURITY WARNING: Please change the default admin password immediately after first login!");
            
        } catch (Exception e) {
            log.error("Failed to seed default admin user", e);
            // Don't fail the application startup, just log the error
        }
    }
}
