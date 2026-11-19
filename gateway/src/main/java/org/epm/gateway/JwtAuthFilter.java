package org.epm.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final SecretKey key;

    public JwtAuthFilter(
            @Value("${app.jwt.secret:dev-only-secret-change-me-dev-only-secret-change-me}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!path.startsWith("/api/") || isPublic(request.getMethod(), path)) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Rejected {} {}: missing bearer token", request.getMethod(), path);
            return unauthorized(exchange);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(header.substring(7))
                    .getPayload();

            ServerHttpRequest authenticated = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .build();
            return chain.filter(exchange.mutate().request(authenticated).build());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Rejected {} {}: invalid or expired token ({})", request.getMethod(), path, e.getClass().getSimpleName());
            return unauthorized(exchange);
        }
    }

    private boolean isPublic(HttpMethod method, String path) {
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }
        if (path.startsWith("/api/user/oauth2/")
                || path.startsWith("/api/user/login/oauth2/")
                || path.startsWith("/api/client/event/")
                || path.equals("/api/event")
                || path.startsWith("/api/event/")
                || path.equals("/api/vendors")
                || path.startsWith("/api/vendors/")
                || path.equals("/api/vendor")
                || path.startsWith("/api/vendor/")) {
            return true;
        }
        if (HttpMethod.GET.equals(method)
                && (path.equals("/api/user") || path.startsWith("/api/user/"))) {
            return true;
        }
        boolean post = HttpMethod.POST.equals(method);
        return post && (path.equals("/api/user/login")
                || path.equals("/api/user")
                || path.equals("/api/user/"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
