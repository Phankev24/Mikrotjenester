package org.example.vendor.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Verifies a Google ID token: signature (Google's public keys), expiry, issuer and audience (our client id). */
@Slf4j
@Component
public class GoogleTokenVerifier {

    public record GoogleUser(String email, String name) {}

    private final String clientId;
    private final NimbusJwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId;
        this.decoder = NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();

        OAuth2TokenValidator<Jwt> issuer = jwt -> {
            String iss = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
            return (iss.equals("https://accounts.google.com") || iss.equals("accounts.google.com"))
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_issuer"));
        };
        OAuth2TokenValidator<Jwt> audience = jwt ->
                jwt.getAudience() != null && jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_audience"));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), issuer, audience));
    }

    public GoogleUser verify(String idToken) {
        if (clientId.isBlank()) {
            log.error("Google login attempted but app.google.client-id is not configured");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google login is not configured");
        }
        try {
            Jwt jwt = decoder.decode(idToken);
            if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")) || jwt.getClaimAsString("email") == null) {
                log.warn("Google login rejected, email is not verified");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
            }
            return new GoogleUser(jwt.getClaimAsString("email"), jwt.getClaimAsString("name"));
        } catch (JwtException e) {
            log.warn("Google login rejected, invalid ID token: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
        }
    }
}