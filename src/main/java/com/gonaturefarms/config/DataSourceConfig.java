package com.gonaturefarms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

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
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        // CRITICAL: Disable prepared statements for Prisma PgBouncer compatibility
        dataSource.addDataSourceProperty("prepareThreshold", "0");

        // Safe startup diagnostics
        String url = dataSourceProperties.getUrl();
        String username = dataSourceProperties.getUsername();
        
        log.info("=== Database Configuration ===");
        log.info("URL Host: {}", extractHost(url));
        log.info("Database: {}", extractDatabase(url));
        log.info("Username: {}", username != null ? username : "NOT SET");
        log.info("SSL Mode: {}", url != null && url.contains("sslmode=require") ? "REQUIRED" : "DEFAULT");
        log.info("PgBouncer: {}", url != null && url.contains("pgbouncer=true") ? "ENABLED" : "DISABLED");
        log.info("=== DataSource Configuration Complete (Prisma PgBouncer Compatible) ===");
        
        return dataSource;
    }

    private String extractHost(String jdbcUrl) {
        if (jdbcUrl == null) return "NOT SET";
        try {
            int start = jdbcUrl.indexOf("://") + 3;
            int end = jdbcUrl.indexOf(":");
            if (start > 3 && end > start) {
                return jdbcUrl.substring(start, end);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return "UNKNOWN";
    }

    private String extractDatabase(String jdbcUrl) {
        if (jdbcUrl == null) return "NOT SET";
        try {
            int lastSlash = jdbcUrl.lastIndexOf("/");
            if (lastSlash > 0 && lastSlash < jdbcUrl.length()) {
                String dbPart = jdbcUrl.substring(lastSlash + 1);
                int queryIndex = dbPart.indexOf("?");
                return queryIndex > 0 ? dbPart.substring(0, queryIndex) : dbPart;
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return "UNKNOWN";
    }
}
