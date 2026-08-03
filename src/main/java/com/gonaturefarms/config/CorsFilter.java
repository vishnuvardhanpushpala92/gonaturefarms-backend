package com.gonaturefarms.config;

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
import java.io.IOException;

/**
 * CORS filter that runs before Spring Security to ensure CORS headers are always present.
 * This is a safety net to ensure CORS works even if Spring Security's CORS configuration fails.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    @Value("${app.frontend-url:*}")
    private String frontendUrl;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String origin = request.getHeader("Origin");
        
        // Allow the Railway frontend URL
        if (origin != null && (frontendUrl == null || frontendUrl.isBlank() || "*".equals(frontendUrl) || origin.equals(frontendUrl))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else if (frontendUrl != null && !frontendUrl.isBlank() && !"*".equals(frontendUrl)) {
            response.setHeader("Access-Control-Allow-Origin", frontendUrl);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // Handle OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
