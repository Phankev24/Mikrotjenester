package org.epm.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException e) {
            log.error("{} {} failed after {} ms: {}", request.getMethod(), request.getRequestURI(),
                    millisSince(start), e.toString(), e);
            throw e;
        }
        int status = response.getStatus();
        long millis = millisSince(start);
        if (status >= 500) {
            log.error("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, millis);
        } else if (status >= 400) {
            log.warn("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, millis);
        } else {
            log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, millis);
        }
    }

    private static long millisSince(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
