package com.gonaturefarms.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.gonaturefarms.security.JwtAuthenticationFilter;
import com.gonaturefarms.util.JsonUtil;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Central security configuration.
 * 
 * Replaces:
 *  - helmet() + cors() in server.js
 *  - middleware/auth.js's requireAuth / requireAdmin
 *  - express-rate-limit
 * 
 * BCrypt is configured at strength 12 to exactly match the cost factor bcryptjs
 * used when hashing passwords in the original Node app.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.frontend-url:https://gonaturefarms-frontend-fvn3mbf18-gonatuefarms.vercel.app,http://localhost:5173,http://localhost:5174}")
    private String frontendUrl;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // private final RateLimitFilter rateLimitFilter; // Temporarily disabled

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> disableJwtFilterAutoRegistration(
            JwtAuthenticationFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter> disableRateLimitFilterAutoRegistration(
            RateLimitFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // stateless JSON API secured by JWT, not cookies
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin())
                    .contentSecurityPolicy(csp -> csp.policyDirectives(
                            "default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline' 'unsafe-eval' fonts.googleapis.com cdn.jsdelivr.net cdnjs.cloudflare.com; " +
                            "style-src 'self' 'unsafe-inline' fonts.googleapis.com; " +
                            "font-src 'self' fonts.gstatic.com; " +
                            "img-src 'self' data: blob: https:; " +
                            "connect-src 'self' cdn.jsdelivr.net blob:; " +
                            "worker-src 'self' blob: cdn.jsdelivr.net; " +
                            "frame-src 'self' https://maps.google.com")))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(this::handleUnauthenticated)
                    .accessDeniedHandler(this::handleForbidden))
            .authorizeHttpRequests(auth -> auth
                    // ── CORS preflight ─────────────────────────────────────────
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // ── Static frontend & health ──────────────────────────────
                    .requestMatchers("/", "/index.html", "/script.js", "/favicon.ico", "/uploads/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/health").permitAll()

                    // ── Public authentication endpoints ───────────────────────
                    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/admin-login").permitAll()
                    .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password", "/api/auth/forgot-password/verify", "/api/auth/reset-password/security-question").permitAll()

                    // ── Public product/category endpoints ─────────────────────
                    .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*", "/api/categories", "/api/categories/*").permitAll()

                    // ── Public order lookup endpoint ─────────────────────
                    .requestMatchers(HttpMethod.GET, "/api/orders/lookup").permitAll()

                    // ── Public admin endpoints (Scrolling Blocks, FAQ, Slides, etc.) ───────────────
                    .requestMatchers(HttpMethod.GET, "/api/admin/settings/public", "/api/admin/slides", "/api/admin/faqs", "/api/videos", "/api/admin/scroll-content").permitAll()

                    // ✅ FIX: Add these to permit public access for Pincode validation and Featured Reviews
                    .requestMatchers(HttpMethod.GET, "/api/admin/zones", "/api/admin/zones/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews", "/api/reviews/**").permitAll()

                    // ── All other API endpoints require authentication ───────────
                    .requestMatchers("/api/**").authenticated()

                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            // .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class); // Temporarily disabled

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (frontendUrl == null || frontendUrl.isBlank() || "*".equals(frontendUrl)) {
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowCredentials(false);
        } else {
            List<String> origins = java.util.Arrays.stream(frontendUrl.split(","))
                    .map(String::trim)
                    .filter(o -> !o.isEmpty())
                    .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                    .toList();
            configuration.setAllowedOrigins(origins);
            configuration.setAllowCredentials(true);
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @SuppressWarnings("unused")
    private void handleUnauthenticated(jakarta.servlet.http.HttpServletRequest _request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException _authException) throws java.io.IOException {
        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
    }

    @SuppressWarnings("unused")
    private void handleForbidden(jakarta.servlet.http.HttpServletRequest _request,
                                  HttpServletResponse response,
                                  org.springframework.security.access.AccessDeniedException _accessDeniedException) throws java.io.IOException {
        writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(JsonUtil.failureJson(message));
    }
}