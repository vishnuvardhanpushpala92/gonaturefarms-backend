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
 * DataSource configuration to handle Render and Railway PostgreSQL databases.
 * 
 * Render provides DATABASE_URL in format: postgresql://user:password@host:port/database
 * Railway provides PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD environment variables
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
        // Detect if running on production platform
        boolean isRender = System.getenv("RENDER") != null || 
                          System.getenv("RENDER_SERVICE_NAME") != null ||
                          System.getenv("RENDER_EXTERNAL_URL") != null;
        
        boolean isRailway = System.getenv("RAILWAY_ENVIRONMENT") != null || 
                           System.getenv("RAILWAY_SERVICE_NAME") != null ||
                           System.getenv("RAILWAY_PROJECT_NAME") != null;
        
        boolean isProduction = isRender || isRailway;
        
        log.info("Environment Detection - Render: {}, Railway: {}, Production: {}", isRender, isRailway, isProduction);
        
        // Read Render/Railway database connection properties
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
        
        log.info("Environment Variables:");
        log.info("DATABASE_URL: {}", databaseUrl != null ? "***" : "null");
        log.info("DB_HOST: {}", dbHost != null ? "***" : "null");
        log.info("DB_PORT: {}", dbPort != null ? "***" : "null");
        log.info("DB_NAME: {}", dbName != null ? "***" : "null");
        log.info("DB_USER: {}", dbUser != null ? "***" : "null");
        log.info("DB_PASSWORD: {}", dbPassword != null ? "***" : "null");
        log.info("PGHOST: {}", pgHost != null ? "***" : "null");
        log.info("PGPORT: {}", pgPort != null ? "***" : "null");
        log.info("PGDATABASE: {}", pgDatabase != null ? "***" : "null");
        log.info("PGUSER: {}", pgUser != null ? "***" : "null");
        log.info("PGPASSWORD: {}", pgPassword != null ? "***" : "null");
        
        // Priority 1: DATABASE_URL in JDBC format (Render provides this)
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("jdbc:postgresql://")) {
            log.info("Using JDBC-format DATABASE_URL directly");
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(databaseUrl);
            config.setUsername(dbUser != null ? dbUser : pgUser);
            config.setPassword(dbPassword != null ? dbPassword : pgPassword);
            config.setDriverClassName("org.postgresql.Driver");
            
            return new HikariDataSource(config);
        }
        
        // Priority 2: DATABASE_URL in postgresql:// format (Render)
        if (databaseUrl != null && !databaseUrl.isEmpty() && databaseUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(databaseUrl);
                
                String username = dbUser != null ? dbUser : pgUser;
                String password = dbPassword != null ? dbPassword : pgPassword;
                
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
                
                log.info("Converted postgresql:// to JDBC URL: jdbc:postgresql://{}:{}{}", 
                         uri.getHost(), 
                         uri.getPort() != -1 ? ":" + uri.getPort() : "", 
                         uri.getPath());
                
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
        
        // Priority 3: Railway PostgreSQL environment variables (PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD)
        if (pgHost != null && !pgHost.isEmpty() && 
            pgPort != null && !pgPort.isEmpty() && 
            pgDatabase != null && !pgDatabase.isEmpty() && 
            pgUser != null && !pgUser.isEmpty() && 
            pgPassword != null && !pgPassword.isEmpty()) {
            
            String jdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;
            log.info("Using Railway PostgreSQL JDBC URL: jdbc:postgresql://{}:{}/{}", pgHost, pgPort, pgDatabase);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(pgUser);
            config.setPassword(pgPassword);
            config.setDriverClassName("org.postgresql.Driver");
            
            return new HikariDataSource(config);
        }
        
        // Priority 4: Individual DB_* variables (Render fallback)
        if (dbHost != null && !dbHost.isEmpty() && 
            dbPort != null && !dbPort.isEmpty() && 
            dbName != null && !dbName.isEmpty() && 
            dbUser != null && !dbUser.isEmpty() && 
            dbPassword != null && !dbPassword.isEmpty()) {
            
            String jdbcUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            log.info("Using individual DB_* JDBC URL: jdbc:postgresql://{}:{}/{}", dbHost, dbPort, dbName);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setDriverClassName("org.postgresql.Driver");
            
            return new HikariDataSource(config);
        }
        
        // Production: Fail fast if no database configuration found
        if (isProduction) {
            String errorMsg = "Running on production (" + (isRender ? "Render" : "Railway") + ") but no valid database configuration found. " +
                              "Required: DATABASE_URL (jdbc:postgresql:// or postgresql:// format) " +
                              "or Railway: PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD " +
                              "or Render: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD. " +
                              "Please check your platform configuration.";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        // Local development only: Use localhost fallback
        log.warn("No database environment variables found, using localhost fallback for local development only");
        String url = "jdbc:postgresql://localhost:5432/gonaturefarms";
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "918252");
        
        log.info("Using local development JDBC URL: {}", url);
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setDriverClassName("org.postgresql.Driver");
        
        return new HikariDataSource(config);
    }
}
