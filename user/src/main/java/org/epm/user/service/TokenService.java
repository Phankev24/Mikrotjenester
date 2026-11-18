package org.epm.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.epm.user.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class TokenService {

    private final SecretKey key;
    private final Duration lifetime;

    public TokenService(
            @Value("${app.jwt.secret:dev-only-secret-change-me-dev-only-secret-change-me}") String secret,
            @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.lifetime = Duration.ofMinutes(expirationMinutes);
    }

    public String issueToken(User user, String provider) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getExternalUserId().toString())
                .claim("username", user.getUsername())
                .claim("provider", provider)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(lifetime)))
                .signWith(key)
                .compact();
    }
}
