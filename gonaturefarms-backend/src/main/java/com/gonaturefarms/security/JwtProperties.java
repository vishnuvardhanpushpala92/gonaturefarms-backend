package com.gonaturefarms.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the app.jwt.* properties from application.properties.
 * Equivalent to process.env.JWT_SECRET / JWT_EXPIRES_IN in the Node app.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HMAC signing secret. Must be at least 256 bits (32 chars) for HS256. */
    private String secret = "GoNatureFarms_SuperSecret_JWT_Key_2026_ChangeInProd!";

    /** Token lifetime, e.g. "7d", "24h", "30m". Mirrors JWT_EXPIRES_IN. */
    private String expiresIn = "7d";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }
}
