package com.gonaturefarms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * DataSource configuration to handle Render's DATABASE_URL format.
 * Render provides DATABASE_URL in format: postgresql://user:password@host:port/database
 * Spring Boot expects JDBC URL format: jdbc:postgresql://host:port/database
 * This configuration parses the Render URL and converts it to JDBC format.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        String username = System.getenv("DATABASE_USERNAME");
        String password = System.getenv("DATABASE_PASSWORD");
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Parse Render's DATABASE_URL format: postgresql://user:password@host:port/database
                URI uri = new URI(databaseUrl);
                
                // Extract username and password from URL if not provided separately
                if ((username == null || username.isEmpty()) && uri.getUserInfo() != null) {
                    String userInfo = uri.getUserInfo();
                    if (userInfo.contains(":")) {
                        String[] parts = userInfo.split(":");
                        username = parts[0];
                        password = parts[1];
                    }
                }
                
                // Convert to JDBC URL format: jdbc:postgresql://host:port/database
                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + 
                                 (uri.getPort() != -1 ? ":" + uri.getPort() : "") + 
                                 uri.getPath();
                
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
                
                return new HikariDataSource(config);
                
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL format: " + databaseUrl, e);
            }
        } else {
            // Fallback to individual environment variables or defaults
            String url = System.getenv().getOrDefault("DATABASE_URL", "jdbc:postgresql://localhost:5432/gonaturefarms");
            String user = System.getenv().getOrDefault("DATABASE_USERNAME", "postgres");
            String pass = System.getenv().getOrDefault("DATABASE_PASSWORD", "918252");
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            config.setDriverClassName("org.postgresql.Driver");
            
            return new HikariDataSource(config);
        }
    }
}
