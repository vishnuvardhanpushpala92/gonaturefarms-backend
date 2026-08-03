package com.gonaturefarms.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * Central security configuration. Replaces:
 *  - helmet() + cors() in server.js (headers / CORS)
 *  - middleware/auth.js's requireAuth / requireAdmin (authentication + authorization)
 *  - express-rate-limit (see {@link RateLimitFilter})
 * <p>
 * BCrypt is configured at strength 12 to exactly match the cost factor bcryptjs used
 * when hashing passwords in the original Node app (bcrypt.hash(password, 12)), so the
 * pre-existing admin password hash seeded by schema.sql keeps working unchanged.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.frontend-url:*}")
    private String frontendUrl;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final CorsFilter corsFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter, CorsFilter corsFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.corsFilter = corsFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthenticationFilter> disableJwtFilterAutoRegistration(
            JwtAuthenticationFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter> disableRateLimitFilterAutoRegistration(
            RateLimitFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<CorsFilter> disableCorsFilterAutoRegistration(
            CorsFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
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

                    // ── Auth (public) ──────────────────────────────────────────
                    .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/admin-login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()

                    // ── Products (public reads, admin writes) ───────────────────
                    .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/products/categories").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/products").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/products/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                    // ── Orders: guest checkout & public tracking allowed ────────
                    .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/orders/lookup").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/orders/my").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/orders/*").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/orders/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/orders/**").authenticated()

                    // ── Wishlist: fully authenticated ───────────────────────────
                    .requestMatchers("/api/wishlist/**").authenticated()

                    // ── Reviews: public reads, authenticated submit, admin moderation ──
                    .requestMatchers(HttpMethod.GET, "/api/reviews/home/featured").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/admin/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/reviews/*").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/reviews/admin/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/reviews/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/reviews/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                    // ── Coupons: public validate, admin CRUD ────────────────────
                    .requestMatchers(HttpMethod.POST, "/api/coupons/validate").permitAll()
                    .requestMatchers("/api/coupons/**").authenticated()

                    // ── Support: public submit, admin manage ────────────────────
                    .requestMatchers(HttpMethod.POST, "/api/support").permitAll()
                    .requestMatchers("/api/support/**").authenticated()

                    // ── Admin module: a handful of public read endpoints ────────
                    .requestMatchers(HttpMethod.GET,
                            "/api/admin/settings/public", "/api/admin/slides", "/api/admin/faqs",
                            "/api/admin/zones", "/api/admin/scroll-content").permitAll()
                    .requestMatchers("/api/admin/**").authenticated()

                    // ── Videos: public reads, admin CRUD ────────────────────────
                    .requestMatchers(HttpMethod.GET, "/api/videos").permitAll()
                    .requestMatchers("/api/videos/**").authenticated()

                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (frontendUrl == null || frontendUrl.isBlank() || "*".equals(frontendUrl)) {
            // Reflect request origin when not explicitly configured, matching the Node app's
            // `origin: process.env.FRONTEND_URL || true` behaviour (wildcard is invalid with credentials).
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            // Use allowedOriginPatterns for specific URLs to ensure proper CORS handling
            configuration.setAllowedOriginPatterns(List.of(frontendUrl));
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void handleUnauthenticated(jakarta.servlet.http.HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException authException) throws java.io.IOException {
        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
    }

    private void handleForbidden(jakarta.servlet.http.HttpServletRequest request,
                                  HttpServletResponse response,
                                  org.springframework.security.access.AccessDeniedException accessDeniedException) throws java.io.IOException {
        writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(JsonUtil.failureJson(message));
    }
}
