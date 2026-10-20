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

        log.info("=== DataSource Configuration ===");
        log.info("Original URL: {}", url);
        log.info("Username: {}", username);

        // Convert postgres:// to jdbc:postgresql:// if needed
        if (url != null && url.startsWith("postgres://")) {
            url = url.replace("postgres://", "jdbc:postgresql://");
            log.info("Converted postgres:// to jdbc:postgresql://");
        }

        // Detect if this is an internal Render connection (no .render.com in hostname)
        boolean isInternalConnection = url != null && !url.contains(".render.com");
        log.info("Connection type: {}", isInternalConnection ? "INTERNAL (no SSL required)" : "EXTERNAL (SSL may be required)");

        // For internal Render connections, explicitly disable SSL to prevent handshake failures
        if (isInternalConnection && url != null) {
            if (!url.contains("sslmode=")) {
                url = url + "?sslmode=disable";
                log.info("Added sslmode=disable for internal connection");
            } else {
                // Replace any existing SSL mode with disable for internal connections
                url = url.replaceAll("sslmode=[^&]*", "sslmode=disable");
                log.info("Replaced sslmode with disable for internal connection");
            }
        }

        // Log the final URL (without password)
        String safeUrl = url != null ? url.replaceAll(":[^:@]+@", ":****@") : null;
        log.info("Final JDBC URL: {}", safeUrl);

        dataSourceProperties.setUrl(url);
        dataSourceProperties.setUsername(username);
        dataSourceProperties.setPassword(password);

        DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        log.info("=== DataSource Configuration Complete ===");
        return dataSource;
    }
}
