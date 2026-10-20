package com.gonaturefarms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * DataSource configuration to handle Render's postgres:// URL format.
 *
 * Render provides DATABASE_URL in postgres:// format, but Spring Boot JDBC
 * requires jdbc:postgresql:// format. This configuration converts it.
 *
 * SSL handling for Render PostgreSQL:
 * - Internal connections (same region): Render injects postgres:// URLs without SSL requirements
 * - External connections (different region): Render injects postgres:// URLs with SSL requirements
 *
 * This configuration preserves the SSL mode from Render's injected URL.
 * For internal connections, we explicitly disable SSL to prevent handshake failures.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private final DataSourceProperties dataSourceProperties;

    public DataSourceConfig(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = dataSourceProperties.getUrl();
        String username = dataSourceProperties.getUsername();
        String password = dataSourceProperties.getPassword();

        log.warn("=== CRITICAL: DataSource Configuration ===");
        log.warn("Original URL: {}", url);
        log.warn("Username: {}", username);

        // FAIL FAST if old Layerbase URL is detected
        if (url != null && (url.toLowerCase().contains("layerbase") || url.contains("gonaturefarms-salt-graph"))) {
            String errorMsg = "CRITICAL ERROR: Old Layerbase database URL detected! This is an obsolete configuration. Please check Render dashboard environment variables and remove any manual DATABASE_URL, DATABASE_USERNAME, or DATABASE_PASSWORD overrides. The render.yaml should inject the correct values from the gonaturefarms-db database service.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        // Convert postgres:// to jdbc:postgresql:// if needed
        if (url != null && url.startsWith("postgres://")) {
            url = url.replace("postgres://", "jdbc:postgresql://");
            log.warn("Converted postgres:// to jdbc:postgresql://");
        }

        // Detect if this is an internal Render connection (no .render.com in hostname)
        boolean isInternalConnection = url != null && !url.contains(".render.com");
        log.warn("Connection type: {}", isInternalConnection ? "INTERNAL (no SSL required)" : "EXTERNAL (SSL may be required)");

        // For internal Render connections, explicitly disable SSL to prevent handshake failures
        if (isInternalConnection && url != null) {
            if (!url.contains("sslmode=")) {
                url = url + "?sslmode=disable";
                log.warn("Added sslmode=disable for internal connection");
            } else {
                // Replace any existing SSL mode with disable for internal connections
                url = url.replaceAll("sslmode=[^&]*", "sslmode=disable");
                log.warn("Replaced sslmode with disable for internal connection");
            }
        }

        // Log the final URL (without password)
        String safeUrl = url != null ? url.replaceAll(":[^:@]+@", ":****@") : null;
        log.warn("Final JDBC URL: {}", safeUrl);

        dataSourceProperties.setUrl(url);
        dataSourceProperties.setUsername(username);
        dataSourceProperties.setPassword(password);

        DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        log.warn("=== DataSource Configuration Complete ===");
        return dataSource;
    }
}
