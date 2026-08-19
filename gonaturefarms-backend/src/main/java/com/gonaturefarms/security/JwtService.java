package com.gonaturefarms.security;

import com.gonaturefarms.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Signs and verifies JWTs, replacing jsonwebtoken's jwt.sign()/jwt.verify() calls
 * from routes/auth.js and middleware/auth.js. Uses the same claim names
 * (id, name, phone, email, role) so tokens carry identical information.
 */
@Service
public class JwtService {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)([smhd])$");

    private final SecretKey signingKey;
    private final Duration expiry;

    public JwtService(JwtProperties jwtProperties) {
        // The secret is padded/hashed via SHA-256 if shorter than 256 bits so any
        // configured value (even a short dev secret) safely satisfies HS256's key-size requirement.
        this.signingKey = buildSigningKey(jwtProperties.getSecret());
        this.expiry = parseDuration(jwtProperties.getExpiresIn());
    }

    private static SecretKey buildSigningKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length >= 32) {
            return Keys.hmacShaKeyFor(bytes);
        }
        return Keys.hmacShaKeyFor(sha256(bytes));
    }

    private static byte[] sha256(byte[] input) {
        try {
            return java.security.MessageDigest.getInstance("SHA-256").digest(input);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Duration parseDuration(String expiresIn) {
        Matcher m = DURATION_PATTERN.matcher(expiresIn.trim());
        if (!m.matches()) {
            return Duration.ofDays(7);
        }
        long value = Long.parseLong(m.group(1));
        return switch (m.group(2)) {
            case "s" -> Duration.ofSeconds(value);
            case "m" -> Duration.ofMinutes(value);
            case "h" -> Duration.ofHours(value);
            case "d" -> Duration.ofDays(value);
            default -> Duration.ofDays(7);
        };
    }

    /** Generates a signed JWT for the given user, mirroring signToken() in routes/auth.js. */
    public String generateToken(Long id, String name, String phone, String email, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiry.toMillis());
        return Jwts.builder()
                .claim("id", id)
                .claim("name", name)
                .claim("phone", phone)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey)
                .compact();
    }

    public String generateToken(User user) {
        return generateToken(user.getId(), user.getName(), user.getPhone(), user.getEmail(),
                user.getRole().name());
    }

    /** Parses and validates a token, throwing JwtException if invalid/expired. */
    public CurrentUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long id = claims.get("id", Number.class) != null
                ? claims.get("id", Number.class).longValue()
                : null;
        return new CurrentUser(
                id,
                claims.get("name", String.class),
                claims.get("phone", String.class),
                claims.get("email", String.class),
                claims.get("role", String.class)
        );
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
