package com.gonaturefarms.config;

import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
        // Read individual database connection properties from environment variables
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        // Check if we have all required environment variables (Render deployment)
        if (dbHost != null && !dbHost.isEmpty() && 
            dbPort != null && !dbPort.isEmpty() && 
            dbName != null && !dbName.isEmpty() && 
            dbUser != null && !dbUser.isEmpty() && 
            dbPassword != null && !dbPassword.isEmpty()) {
            
            // Build JDBC URL from individual properties
            String jdbcUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName("org.postgresql.Driver");
            
            return new HikariDataSource(config);
        }
        
        // Fallback: Try DATABASE_URL format (for compatibility with other setups)
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Parse DATABASE_URL format: postgresql://user:password@host:port/database
                URI uri = new URI(databaseUrl);
                
                String username = dbUser;
                String password = dbPassword;
                
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
        }
        
        // For local development, use localhost fallback
        String url = "jdbc:postgresql://localhost:5432/gonaturefarms";
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "918252");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName("org.postgresql.Driver");
        
        return new HikariDataSource(config);
    }
}
