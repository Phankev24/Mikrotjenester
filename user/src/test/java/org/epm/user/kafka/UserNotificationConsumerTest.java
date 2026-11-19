package org.epm.user.kafka;

import org.epm.user.TestData;
import org.epm.user.notification.Notification;
import org.epm.user.notification.NotificationRepo;
import org.epm.user.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationConsumerTest {

    private static final UUID ORGANIZER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID EVENT_ID = UUID.fromString("3f2c1b7a-0000-4000-8000-000000000001");
    private static final UUID ATTENDEE_ID = UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90");
    private static final Instant JOINED_AT = Instant.parse("2026-11-18T10:15:30Z");

    @Mock
    private UserRepo userRepo;
    @Mock
    private NotificationRepo notificationRepo;

    private UserNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserNotificationConsumer(JsonMapper.builder().build(), userRepo, notificationRepo);
    }

    private static String payload(UUID organizerId, String attendeeName, String attendeeEmail) {
        return """
                {"externalEventId":"%s","eventName":"Summer Picnic","organizerId":"%s",
                 "attendeeUserId":"%s","attendeeName":%s,"attendeeEmail":%s,
                 "joinedAt":"2026-11-18T10:15:30Z"}
                """.formatted(EVENT_ID, organizerId, ATTENDEE_ID,
                attendeeName == null ? "null" : "\"" + attendeeName + "\"",
                attendeeEmail == null ? "null" : "\"" + attendeeEmail + "\"");
    }

    private void organizerIsAUser() {
        when(userRepo.findByExternalUserId(ORGANIZER_ID))
                .thenReturn(Optional.of(TestData.user(1L, ORGANIZER_ID, "ola")));
    }

    @Test
    void storesANotificationWithNameAndEmailForTheOrganizer() {
        organizerIsAUser();

        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", "kari@example.com"));

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(saved.capture());
        Notification notification = saved.getValue();
        assertThat(notification.getMessage()).isEqualTo("Kari Nordmann (kari@example.com) joined your event 'Summer Picnic'");
        assertThat(notification.getExternalUserId()).isEqualTo(ORGANIZER_ID);
        assertThat(notification.getExternalEventId()).isEqualTo(EVENT_ID);
        assertThat(notification.getEventName()).isEqualTo("Summer Picnic");
        assertThat(notification.getAttendeeUserId()).isEqualTo(ATTENDEE_ID);
        assertThat(notification.getJoinedAt()).isEqualTo(JOINED_AT);
    }

    @Test
    void leavesOutTheEmailWhenItIsMissingOrBlank() {
        organizerIsAUser();

        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", null));
        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", " "));

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(Notification::getMessage)
                .containsOnly("Kari Nordmann joined your event 'Summer Picnic'");
    }

    @Test
    void ignoresEventsWhoseOrganizerIsNotAUser() {
        UUID vendorOrganizer = UUID.randomUUID();
        when(userRepo.findByExternalUserId(vendorOrganizer)).thenReturn(Optional.empty());

        consumer.onEventJoined(payload(vendorOrganizer, "Kari Nordmann", "kari@example.com"));

        verify(notificationRepo, never()).save(any());
    }

    @Test
    void dropsAMalformedPayloadInsteadOfCrashingTheListener() {
        consumer.onEventJoined("this is not json");

        verify(notificationRepo, never()).save(any());
    }

    @Test
    void doesNotStoreTheSameJoinTwice() {
        organizerIsAUser();
        when(notificationRepo.existsByExternalEventIdAndAttendeeUserIdAndJoinedAt(EVENT_ID, ATTENDEE_ID, JOINED_AT))
                .thenReturn(true);

        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", "kari@example.com"));

        verify(notificationRepo, never()).save(any());
    }

    @Test
    void ignoresADuplicateThatSlipsThroughTheCheck() {
        organizerIsAUser();
        when(notificationRepo.save(any(Notification.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", "kari@example.com"));

        verify(notificationRepo).save(any(Notification.class));
    }

    @Test
    void letsDatabaseFailuresPropagateSoKafkaRedeliversTheMessage() {
        organizerIsAUser();
        when(notificationRepo.save(any(Notification.class))).thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", "kari@example.com")))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void returnsTheStoredNotificationsOfAUser() {
        Notification stored = new Notification();
        stored.setExternalUserId(ORGANIZER_ID);
        stored.setExternalEventId(EVENT_ID);
        stored.setEventName("Summer Picnic");
        stored.setAttendeeUserId(ATTENDEE_ID);
        stored.setMessage("Kari Nordmann joined your event 'Summer Picnic'");
        stored.setJoinedAt(JOINED_AT);
        when(notificationRepo.findByExternalUserIdOrderByJoinedAtAscIdAsc(ORGANIZER_ID)).thenReturn(List.of(stored));

        List<UserNotificationConsumer.EventJoinedNotification> notifications = consumer.getNotificationsForUser(ORGANIZER_ID);

        assertThat(notifications).singleElement().satisfies(notification -> {
            assertThat(notification.externalEventId()).isEqualTo(EVENT_ID);
            assertThat(notification.eventName()).isEqualTo("Summer Picnic");
            assertThat(notification.attendeeUserId()).isEqualTo(ATTENDEE_ID);
            assertThat(notification.message()).isEqualTo("Kari Nordmann joined your event 'Summer Picnic'");
            assertThat(notification.joinedAt()).isEqualTo(JOINED_AT);
        });
    }

    @Test
    void returnsAnEmptyListForUsersWithoutNotifications() {
        assertThat(consumer.getNotificationsForUser(UUID.randomUUID())).isEmpty();
    }
}
