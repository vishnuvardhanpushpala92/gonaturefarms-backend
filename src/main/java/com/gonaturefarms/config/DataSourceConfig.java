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
 * DataSource configuration for Prisma PostgreSQL.
 * Ensures connection pooling compatibility with PgBouncer.
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
        log.info("URL (masked): {}", url != null ? url.replaceAll(":[^:@]+@", ":****@") : null);
        log.info("Username: {}", username);
        log.info("SSL mode: {}", url != null && url.contains("sslmode=require") ? "REQUIRED (Prisma)" : "DISABLED/DEFAULT");

        dataSourceProperties.setUrl(url);
        dataSourceProperties.setUsername(username);
        dataSourceProperties.setPassword(password);

        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        // CRITICAL: Disable prepared statements for Prisma PgBouncer compatibility
        dataSource.addDataSourceProperty("prepareThreshold", "0");

        log.info("=== DataSource Configuration Complete ===");
        return dataSource;
    }
}