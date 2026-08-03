package com.gonaturefarms.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CORS filter that runs before Spring Security to ensure CORS headers are always present.
 * This is a safety net to ensure CORS works even if Spring Security's CORS configuration fails.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    @Value("${app.frontend-url:*}")
    private String frontendUrl;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        logger.debug("CORS Filter - Method: {}, Path: {}, Origin: {}, Configured Frontend URL: {}", 
                     method, path, origin, frontendUrl);
        
        // Always set CORS headers for all requests
        String allowOrigin;
        if (origin != null) {
            // If origin matches configured frontend or we're in dev mode (wildcard), allow it
            if (frontendUrl == null || frontendUrl.isBlank() || "*".equals(frontendUrl) || origin.equals(frontendUrl)) {
                allowOrigin = origin;
            } else {
                // In production, allow the configured frontend URL
                // Also allow the specific Railway frontend URL as a safety fallback
                allowOrigin = frontendUrl;
            }
        } else {
            // No origin header (same-origin or non-browser request)
            // Allow the configured frontend URL or wildcard in dev mode
            if (frontendUrl != null && !frontendUrl.isBlank() && !"*".equals(frontendUrl)) {
                allowOrigin = frontendUrl;
            } else {
                allowOrigin = "*";
            }
        }
        
        // Safety fallback: If the origin is the Railway frontend, always allow it
        if (origin != null && origin.contains("railway.app")) {
            allowOrigin = origin;
        }
        
        response.setHeader("Access-Control-Allow-Origin", allowOrigin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        logger.debug("CORS Filter - Set Access-Control-Allow-Origin: {}", allowOrigin);
        
        // Handle OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("CORS Filter - Handling OPTIONS preflight request");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
