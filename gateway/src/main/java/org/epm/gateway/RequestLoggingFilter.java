package org.epm.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }
        long start = System.nanoTime();
        return chain.filter(exchange)
                .doOnSuccess(done -> logResult(exchange, method, path, start))
                .doOnError(error -> log.error("{} {} failed after {} ms: {}", method, path,
                        millisSince(start), error.toString()));
    }

    private void logResult(ServerWebExchange exchange, String method, String path, long start) {
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        int status = statusCode != null ? statusCode.value() : 200;
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI target = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String routeId = route != null ? route.getId() : "none";
        String instance = target != null ? target.getHost() + ":" + target.getPort() : "none";
        long millis = millisSince(start);
        if (status >= 500) {
            log.error("{} {} -> {} via route {} to {} ({} ms)", method, path, status, routeId, instance, millis);
        } else if (status >= 400) {
            log.warn("{} {} -> {} via route {} to {} ({} ms)", method, path, status, routeId, instance, millis);
        } else {
            log.info("{} {} -> {} via route {} to {} ({} ms)", method, path, status, routeId, instance, millis);
        }
    }

    private static long millisSince(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
