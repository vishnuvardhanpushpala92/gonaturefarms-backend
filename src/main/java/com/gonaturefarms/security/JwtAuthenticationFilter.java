package com.gonaturefarms.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Equivalent to middleware/auth.js's requireAuth logic, but applied globally as a
 * stateless filter: if a valid "Authorization: Bearer &lt;token&gt;" header is present,
 * the resulting CurrentUser is placed into the SecurityContext. Requests without a
 * token, or with an invalid one, simply proceed unauthenticated — endpoint-level
 * access rules (see SecurityConfig / @PreAuthorize) decide whether that's allowed.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                CurrentUser user = jwtService.parseToken(token);
                SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(user));
            } catch (JwtException | IllegalArgumentException ex) {
                // Invalid/expired token: leave unauthenticated. Protected endpoints will
                // then correctly respond 401 via Spring Security's entry point, mirroring
                // requireAuth's "Invalid or expired token" response.
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
