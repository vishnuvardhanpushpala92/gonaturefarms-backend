package com.gonaturefarms.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Convenience accessor for the current request's decoded JWT principal. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<CurrentUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CurrentUser user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    /** Returns the current user or throws if unauthenticated (should not happen behind requireAuth()). */
    public static CurrentUser requireCurrentUser() {
        return getCurrentUser().orElseThrow(() ->
                new org.springframework.security.access.AccessDeniedException("Authentication required"));
    }
}
