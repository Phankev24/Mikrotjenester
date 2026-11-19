package org.epm.event.kafka;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventKafkaProducerTest {

    private static final UUID EVENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ORGANIZER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID ATTENDEE_ID = UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90");

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        logger = (Logger) LoggerFactory.getLogger(EventKafkaProducer.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
    }

    @Test
    void publishesTheJoinMessageAsJsonOnTheEventJoinedTopicKeyedByEventId() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        publish();

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("event-joined"), eq(EVENT_ID.toString()), json.capture());
        assertThat(json.getValue())
                .contains("\"eventName\":\"Summer Picnic\"")
                .contains("\"organizerId\":\"" + ORGANIZER_ID + "\"")
                .contains("\"attendeeUserId\":\"" + ATTENDEE_ID + "\"")
                .contains("\"attendeeEmail\":\"kari@example.com\"");
    }

    @Test
    void logsAtInfoWhenTheBrokerAcceptsTheMessage() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        publish();

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.INFO);
            assertThat(event.getFormattedMessage()).contains(EVENT_ID.toString()).contains("event-joined");
        });
    }

    @Test
    void logsAtErrorWhenTheSendFails() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker down")));

        publish();

        assertThat(appender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.ERROR);
            assertThat(event.getFormattedMessage()).contains("Failed to publish").contains(EVENT_ID.toString());
        });
    }

    private void publish() {
        new EventKafkaProducer(kafkaTemplate, JsonMapper.builder().build()).publishEventJoined(new EventJoinedEvent(
                EVENT_ID, "Summer Picnic", ORGANIZER_ID, ATTENDEE_ID, "Kari Nordmann", "kari@example.com",
                Instant.parse("2026-11-18T10:15:30Z")));
    }
}
