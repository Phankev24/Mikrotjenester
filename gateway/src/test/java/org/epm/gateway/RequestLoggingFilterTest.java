package org.epm.gateway;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
    }

    @Test
    void logsTheStatusTheRouteAndTheInstanceThatServedTheCall() {
        MockServerWebExchange exchange = exchange("/api/user");
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR,
                Route.async().id("user").uri("lb://user").predicate(e -> true).build());
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR,
                URI.create("http://172.24.0.9:8081/api/user"));

        filter.filter(exchange, respondingWith(HttpStatus.OK)).block();

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage())
                    .startsWith("GET /api/user -> 200 via route user to 172.24.0.9:8081 (");
        });
    }

    @Test
    void logsClientErrorsAtWarn() {
        filter.filter(exchange("/api/user/1"), respondingWith(HttpStatus.NOT_FOUND)).block();

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).startsWith("GET /api/user/1 -> 404 via route none to none (");
        });
    }

    @Test
    void logsServerErrorsAtError() {
        filter.filter(exchange("/api/event"), respondingWith(HttpStatus.SERVICE_UNAVAILABLE)).block();

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).startsWith("GET /api/event -> 503");
        });
    }

    @Test
    void logsAndPropagatesFailuresFromTheChain() {
        GatewayFilterChain failing = exchange -> Mono.error(new IllegalStateException("connection refused"));

        assertThatThrownBy(() -> filter.filter(exchange("/api/event"), failing).block())
                .isInstanceOf(IllegalStateException.class);

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).contains("GET /api/event failed after").contains("connection refused");
        });
    }

    @Test
    void doesNotLogHealthChecks() {
        filter.filter(exchange("/actuator/health"), respondingWith(HttpStatus.OK)).block();

        assertThat(appender.list).isEmpty();
    }

    private static MockServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    private static GatewayFilterChain respondingWith(HttpStatus status) {
        return exchange -> {
            exchange.getResponse().setStatusCode(status);
            return Mono.empty();
        };
    }
}
