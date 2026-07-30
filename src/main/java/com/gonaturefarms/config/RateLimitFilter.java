package com.gonaturefarms.config;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.gonaturefarms.util.JsonUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A minimal in-memory, fixed-window rate limiter that mirrors the two limiters
 * configured with express-rate-limit in the original server.js:
 *  - general: 300 requests / 15 minutes per IP on /api/**
 *  - auth:    20 requests / 15 minutes per IP on /api/auth/**
 * <p>
 * This is per-instance state, which is fine for a single-node deployment. For a
 * multi-instance / horizontally scaled deployment, replace the in-memory counters
 * with a shared store such as Redis (e.g. via Bucket4j + Redis).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 15 * 60 * 1000L;
    private static final int GENERAL_LIMIT = 300;
    private static final int AUTH_LIMIT = 20;

    private final ConcurrentHashMap<String, Window> generalWindows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Window> authWindows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = clientKey(request);

        if (path.startsWith("/api/auth/") && exceeded(authWindows, clientKey, AUTH_LIMIT)) {
            respondTooManyRequests(response, "Too many auth attempts, please try again later.");
            return;
        }
        if (exceeded(generalWindows, clientKey, GENERAL_LIMIT)) {
            respondTooManyRequests(response, "Too many requests, please try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean exceeded(ConcurrentHashMap<String, Window> store, String key, int limit) {
        long now = System.currentTimeMillis();
        Window window = store.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MILLIS) {
                return new Window(now);
            }
            return existing;
        });
        return window.count.incrementAndGet() > limit;
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

  private void respondTooManyRequests(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType("application/json");
    response.getWriter().write(JsonUtil.failureJson(message));
}

    private static final class Window {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
