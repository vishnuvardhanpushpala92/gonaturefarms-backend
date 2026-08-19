package com.gonaturefarms.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/** Spring Security Authentication implementation wrapping a decoded JWT's CurrentUser. */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final CurrentUser principal;

    public JwtAuthenticationToken(CurrentUser principal) {
        super(authorities(principal));
        this.principal = principal;
        setAuthenticated(true);
    }

    private static List<GrantedAuthority> authorities(CurrentUser principal) {
        String role = principal.role() == null ? "customer" : principal.role();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
