package com.gonaturefarms.security;

/**
 * Lightweight representation of the JWT payload, equivalent to the object the
 * original Express app attached to {@code req.user} after verifying the token
 * ({id, name, phone, email, role}).
 */
public record CurrentUser(Long id, String name, String phone, String email, String role) {

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
