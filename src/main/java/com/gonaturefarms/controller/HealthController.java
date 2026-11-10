package com.gonaturefarms.controller;

import com.gonaturefarms.dto.common.ApiResponse;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

/** GET /api/health — liveness and readiness check including database connectivity. */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ApiResponse health() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check database connectivity
            long dbStart = System.currentTimeMillis();
            String dbResult = jdbcTemplate.queryForObject("SELECT 1", String.class);
            long dbEnd = System.currentTimeMillis();
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            // Get HikariCP pool metrics if available
            int activeConnections = 0;
            int idleConnections = 0;
            int totalConnections = 0;
            
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
                activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
                idleConnections = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
                totalConnections = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
            }
            
            log.info("Health check passed - DB query: {}ms, Pool: {}/{}/{}, Total: {}ms", 
                (dbEnd - dbStart), activeConnections, idleConnections, totalConnections, totalTime);
            
            return ApiResponse.ok("Go Nature Farms API is running")
                    .with("timestamp", java.time.Instant.now())
                    .with("database", "connected")
                    .with("databaseLatencyMs", (dbEnd - dbStart))
                    .with("poolActiveConnections", activeConnections)
                    .with("poolIdleConnections", idleConnections)
                    .with("poolTotalConnections", totalConnections)
                    .with("totalLatencyMs", totalTime);
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            log.error("Health check failed - Total: {}ms", totalTime, e);
            
            return ApiResponse.fail("Health check failed: " + e.getMessage())
                    .with("timestamp", java.time.Instant.now())
                    .with("database", "disconnected")
                    .with("error", e.getClass().getSimpleName())
                    .with("totalLatencyMs", totalTime);
        }
    }
}
