package com.gonaturefarms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * DataSource configuration to handle Render's postgres:// and postgresql:// URL formats.
 * 
 * Render provides DATABASE_URL in a standard URI format, but Spring Boot JDBC
 * requires the jdbc:postgresql:// format. This configuration safely parses the URL,
 * constructs the proper JDBC string, and disables SSL for internal Render connections.
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

        // Handle both postgres:// and postgresql:// formats provided by Render
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                // Use URI to safely parse the connection string
                URI dbUri = new URI(url);
                
                // Extract username and password from the URL if they exist
                if (dbUri.getUserInfo() != null) {
                    String[] userInfo = dbUri.getUserInfo().split(":");
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }

                // Ensure port 5432 is included and force sslmode=disable
                url = "jdbc:postgresql://" + dbUri.getHost() + ":5432" + dbUri.getPath() + "?sslmode=disable";
                log.info("Successfully parsed and converted URL to JDBC format.");

            } catch (URISyntaxException e) {
                log.error("Failed to parse DATABASE_URL", e);
                throw new RuntimeException("Invalid DATABASE_URL format", e);
            }
        } else if (url != null && url.startsWith("jdbc:postgresql://")) {
            // If it's already a JDBC URL, just ensure SSL is disabled
            if (!url.contains("sslmode=")) {
                url = url + (url.contains("?") ? "&" : "?") + "sslmode=disable";
            } else {
                url = url.replaceAll("sslmode=[^&]*", "sslmode=disable");
            }
            log.info("URL is already in JDBC format. Ensured sslmode=disable.");
        }

        // Log the final URL (masking the password for security)
        String safeUrl = url != null ? url.replaceAll(":[^:@]+@", ":****@") : null;
        log.info("Final JDBC URL: {}", safeUrl);
        log.info("Final Username: {}", username);

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