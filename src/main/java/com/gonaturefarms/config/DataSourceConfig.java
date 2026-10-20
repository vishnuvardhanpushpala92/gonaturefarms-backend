package com.gonaturefarms.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;

/**
 * DataSource configuration to handle Render's postgres:// URL format.
 * 
 * Render provides DATABASE_URL in postgres:// format, but Spring Boot JDBC
 * requires jdbc:postgresql:// format. This configuration converts it
 * and ensures SSL is disabled for internal Render database connections.
 */
@Configuration
public class DataSourceConfig {

    private final DataSourceProperties dataSourceProperties;

    public DataSourceConfig(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    @PostConstruct
    public void configureDataSourceUrl() {
        String url = dataSourceProperties.getUrl();
        if (url != null && url.startsWith("postgres://")) {
            // Convert postgres:// to jdbc:postgresql://
            String jdbcUrl = url.replace("postgres://", "jdbc:postgresql://");
            
            // Force SSL to be disabled for internal Render connections
            if (!jdbcUrl.contains("sslmode=")) {
                jdbcUrl = jdbcUrl + "?sslmode=disable";
            } else {
                // Replace any existing SSL mode with disable
                jdbcUrl = jdbcUrl.replaceAll("sslmode=[^&]*", "sslmode=disable");
            }
            
            dataSourceProperties.setUrl(jdbcUrl);
        }
    }
}
