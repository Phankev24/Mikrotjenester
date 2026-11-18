package org.epm.user.kafka;

import org.epm.user.TestData;
import org.epm.user.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserNotificationConsumerTest {

    private static final UUID ORGANIZER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID EVENT_ID = UUID.fromString("3f2c1b7a-0000-4000-8000-000000000001");
    private static final UUID ATTENDEE_ID = UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90");

    @Mock
    private UserRepo userRepo;

    private UserNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserNotificationConsumer(JsonMapper.builder().build(), userRepo);
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

    @Test
    void storesANotificationWithNameAndEmailForTheOrganizer() {
        when(userRepo.findByExternalUserId(ORGANIZER_ID))
                .thenReturn(Optional.of(TestData.user(1L, ORGANIZER_ID, "ola")));

        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", "kari@example.com"));

        List<UserNotificationConsumer.EventJoinedNotification> notifications = consumer.getNotificationsForUser(ORGANIZER_ID);
        assertThat(notifications).hasSize(1);
        UserNotificationConsumer.EventJoinedNotification notification = notifications.get(0);
        assertThat(notification.message()).isEqualTo("Kari Nordmann (kari@example.com) joined your event 'Summer Picnic'");
        assertThat(notification.externalEventId()).isEqualTo(EVENT_ID);
        assertThat(notification.attendeeUserId()).isEqualTo(ATTENDEE_ID);
        assertThat(notification.joinedAt()).isNotNull();
    }

    @Test
    void leavesOutTheEmailWhenItIsMissingOrBlank() {
        when(userRepo.findByExternalUserId(ORGANIZER_ID))
                .thenReturn(Optional.of(TestData.user(1L, ORGANIZER_ID, "ola")));

        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", null));
        consumer.onEventJoined(payload(ORGANIZER_ID, "Kari Nordmann", " "));

        assertThat(consumer.getNotificationsForUser(ORGANIZER_ID))
                .extracting(UserNotificationConsumer.EventJoinedNotification::message)
                .containsOnly("Kari Nordmann joined your event 'Summer Picnic'");
    }

    @Test
    void ignoresEventsWhoseOrganizerIsNotAUser() {
        UUID vendorOrganizer = UUID.randomUUID();
        when(userRepo.findByExternalUserId(vendorOrganizer)).thenReturn(Optional.empty());

        consumer.onEventJoined(payload(vendorOrganizer, "Kari Nordmann", "kari@example.com"));

        assertThat(consumer.getNotificationsForUser(vendorOrganizer)).isEmpty();
    }

    @Test
    void swallowsAMalformedPayloadInsteadOfCrashingTheListener() {
        consumer.onEventJoined("this is not json");

        assertThat(consumer.getNotificationsForUser(ORGANIZER_ID)).isEmpty();
    }

    @Test
    void returnsAnEmptyListForUsersWithoutNotifications() {
        assertThat(consumer.getNotificationsForUser(UUID.randomUUID())).isEmpty();
    }
}
