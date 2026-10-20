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
 * requires jdbc:postgresql:// format. This configuration creates a custom
 * DataSource bean with the converted URL and SSL disabled for internal
 * Render database connections.
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

        // Force SSL to be disabled for internal Render connections
        if (url != null && !url.contains("sslmode=")) {
            url = url + "?sslmode=disable";
            log.info("Added sslmode=disable to URL");
        } else if (url != null) {
            // Replace any existing SSL mode with disable
            url = url.replaceAll("sslmode=[^&]*", "sslmode=disable");
            log.info("Replaced existing sslmode with disable");
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
