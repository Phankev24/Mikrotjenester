package org.epm.user.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
    void logsSuccessfulCallsAtInfo() throws Exception {
        call("GET", "/api/user", 200);

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).startsWith("GET /api/user -> 200 (");
        });
    }

    @Test
    void logsClientErrorsAtWarn() throws Exception {
        call("POST", "/api/user/login", 401);

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).startsWith("POST /api/user/login -> 401 (");
        });
    }

    @Test
    void logsServerErrorsAtError() throws Exception {
        call("DELETE", "/api/user/1", 500);

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).startsWith("DELETE /api/user/1 -> 500 (");
        });
    }

    @Test
    void logsAndRethrowsUnhandledExceptions() {
        FilterChain failing = (request, response) -> {
            throw new ServletException("boom");
        };

        assertThatThrownBy(() -> filter.doFilter(new MockHttpServletRequest("GET", "/api/user"),
                new MockHttpServletResponse(), failing)).isInstanceOf(ServletException.class);

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).contains("GET /api/user failed after").contains("boom");
        });
    }

    @Test
    void doesNotLogHealthChecks() throws Exception {
        call("GET", "/actuator/health", 200);

        assertThat(appender.list).isEmpty();
    }

    private void call(String method, String uri, int status) throws Exception {
        filter.doFilter(new MockHttpServletRequest(method, uri), new MockHttpServletResponse(),
                (request, response) -> ((HttpServletResponse) response).setStatus(status));
    }
}
