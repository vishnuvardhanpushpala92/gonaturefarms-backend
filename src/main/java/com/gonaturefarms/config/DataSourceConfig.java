package com.gonaturefarms.config;

import java.net.URI;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DataSource configuration to handle Railway and Render PostgreSQL databases.
 * 
 * Railway provides DATABASE_URL in format: postgres://user:password@host:port/database
 * Railway also provides PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD environment variables
 * Render provides DATABASE_URL in format: postgresql://user:password@host:port/database
 * 
 * Spring Boot expects JDBC URL format: jdbc:postgresql://host:port/database
 * This configuration parses the platform-specific formats and converts to JDBC format.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        // Read Railway/Render database connection properties
        String databaseUrl = System.getenv("DATABASE_URL");
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        // Read Railway-specific PostgreSQL environment variables
        String pgHost = System.getenv("PGHOST");
        String pgPort = System.getenv("PGPORT");
        String pgDatabase = System.getenv("PGDATABASE");
        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");
        
        log.info("Initializing DataSource configuration");
        log.info("DATABASE_URL: {}", databaseUrl != null ? "***" : "null");
        log.info("PGHOST: {}", pgHost != null ? "***" : "null");
        log.info("PGPORT: {}", pgPort != null ? "***" : "null");
        log.info("PGDATABASE: {}", pgDatabase != null ? "***" : "null");
        log.info("PGUSER: {}", pgUser != null ? "***" : "null");
        log.info("PGPASSWORD: {}", pgPassword != null ? "***" : "null");
        
        String jdbcUrl = null;
        String username = null;
        String password = null;
        
        // Priority 1: DATABASE_URL in JDBC format (jdbc:postgresql://)
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("jdbc:postgresql://")) {
            log.info("Using JDBC-format DATABASE_URL directly");
            jdbcUrl = databaseUrl;
            username = pgUser != null ? pgUser : dbUser;
            password = pgPassword != null ? pgPassword : dbPassword;
        }
        // Priority 2: DATABASE_URL in postgres:// format (Railway)
        else if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgres://")) {
            try {
                log.info("Converting Railway postgres:// to JDBC format");
                URI uri = new URI(databaseUrl);
                
                // Extract username and password from URL
                if (uri.getUserInfo() != null) {
                    String userInfo = uri.getUserInfo();
                    if (userInfo.contains(":")) {
                        String[] parts = userInfo.split(":");
                        username = parts[0];
                        password = parts[1];
                    }
                }
                
                // Convert to JDBC URL format: jdbc:postgresql://host:port/database
                jdbcUrl = "jdbc:postgresql://" + uri.getHost() + 
                         (uri.getPort() != -1 ? ":" + uri.getPort() : "") + 
                         uri.getPath();
                
                log.info("Converted postgres:// to JDBC URL: jdbc:postgresql://{}:{}{}", 
                         uri.getHost(), 
                         uri.getPort() != -1 ? ":" + uri.getPort() : "", 
                         uri.getPath());
                
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL format: " + databaseUrl, e);
            }
        }
        // Priority 3: DATABASE_URL in postgresql:// format (Render)
        else if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgresql://")) {
            try {
                log.info("Converting Render postgresql:// to JDBC format");
                URI uri = new URI(databaseUrl);
                
                // Extract username and password from URL
                if (uri.getUserInfo() != null) {
                    String userInfo = uri.getUserInfo();
                    if (userInfo.contains(":")) {
                        String[] parts = userInfo.split(":");
                        username = parts[0];
                        password = parts[1];
                    }
                }
                
                // Convert to JDBC URL format: jdbc:postgresql://host:port/database
                jdbcUrl = "jdbc:postgresql://" + uri.getHost() + 
                         (uri.getPort() != -1 ? ":" + uri.getPort() : "") + 
                         uri.getPath();
                
                log.info("Converted postgresql:// to JDBC URL: jdbc:postgresql://{}:{}{}", 
                         uri.getHost(), 
                         uri.getPort() != -1 ? ":" + uri.getPort() : "", 
                         uri.getPath());
                
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL format: " + databaseUrl, e);
            }
        }
        // Priority 4: Railway PostgreSQL environment variables (PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD)
        else if (pgHost != null && !pgHost.isEmpty() && 
                 pgPort != null && !pgPort.isEmpty() && 
                 pgDatabase != null && !pgDatabase.isEmpty() && 
                 pgUser != null && !pgUser.isEmpty() && 
                 pgPassword != null && !pgPassword.isEmpty()) {
            
            log.info("Using Railway PG_* environment variables");
            jdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;
            username = pgUser;
            password = pgPassword;
        }
        // Priority 5: Individual DB_* variables (Render fallback)
        else if (dbHost != null && !dbHost.isEmpty() && 
                 dbPort != null && !dbPort.isEmpty() && 
                 dbName != null && !dbName.isEmpty() && 
                 dbUser != null && !dbUser.isEmpty() && 
                 dbPassword != null && !dbPassword.isEmpty()) {
            
            log.info("Using Render DB_* environment variables");
            jdbcUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            username = dbUser;
            password = dbPassword;
        }
        // Priority 6: Local development fallback
        else {
            log.warn("No production database environment variables found, using localhost for local development");
            jdbcUrl = "jdbc:postgresql://localhost:5432/gonaturefarms";
            username = "postgres";
            password = "918252";
        }
        
        // Validate that we have a valid JDBC URL
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            throw new RuntimeException("Failed to construct JDBC URL. Please set DATABASE_URL or PG_* environment variables.");
        }
        
        // Validate that username and password are set
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("Database username is not set. Please set PGUSER or DB_USER environment variable.");
        }
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Database password is not set. Please set PGPASSWORD or DB_PASSWORD environment variable.");
        }
        
        log.info("Configuring HikariCP with JDBC URL: jdbc:postgresql://***:port/database");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        
        // HikariCP connection pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
    }
}
