package org.example.vendor.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(ApiExceptionHandler.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
    }

    @Test
    void clientErrorsAreLoggedAtWarnWithTheReason() {
        ProblemDetail problem = handler.handleStatus(new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).contains("404").contains("Vendor not found");
        });
    }

    @Test
    void serverErrorsAreLoggedAtError() {
        ProblemDetail problem = handler.handleStatus(
                new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Event service returned no event"));

        assertThat(problem.getStatus()).isEqualTo(502);
        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).contains("502").contains("Event service returned no event");
        });
    }

    @Test
    void integrityViolationsAreLoggedAtWarnAndAnswered409() {
        ProblemDetail problem = handler.handleIntegrity(new DataIntegrityViolationException("duplicate"));

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage()).contains("409");
        });
    }
}
