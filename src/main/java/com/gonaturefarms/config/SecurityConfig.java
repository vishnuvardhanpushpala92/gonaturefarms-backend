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

    // RateLimitFilter is a @Component, so Spring Boot auto-registers it as a raw
    // servlet filter on every request by default. The bean below was previously
    // commented out while addFilterBefore(rateLimitFilter, ...) below was ALSO
    // commented out — leaving RateLimitFilter running uncontrolled outside the
    // Spring Security chain (same double-registration class of bug that
    // disableJwtFilterAutoRegistration exists to prevent for the JWT filter).
    // Re-enabled here so its state matches the "temporarily disabled" intent:
    // fully inert until someone deliberately re-wires it with addFilterBefore.
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
            .cors(org.springframework.security.config.Customizer.withDefaults())
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
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            // .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class); // Temporarily disabled

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (frontendUrl == null || frontendUrl.isBlank() || "*".equals(frontendUrl)) {
            // Reflect request origin when not explicitly configured
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowCredentials(false);
        } else {
            // FRONTEND_URL supports a comma-separated list (e.g. Netlify prod domain +
            // local dev, or prod + a preview deploy) so one missing/extra origin
            // doesn't take every client down. Each entry is trimmed and any trailing
            // slash stripped, since Spring's origin match is an exact string comparison
            // and a copy-pasted "https://app.example.com/" (trailing slash) will never
            // match the browser's actual Origin header ("https://app.example.com"),
            // silently producing a 403 on every cross-origin request.
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