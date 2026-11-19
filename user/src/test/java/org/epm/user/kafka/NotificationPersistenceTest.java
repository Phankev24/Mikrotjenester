package org.epm.user.kafka;

import org.epm.user.IntegrationTestBase;
import org.epm.user.notification.Notification;
import org.epm.user.notification.NotificationRepo;
import org.epm.user.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationPersistenceTest extends IntegrationTestBase {

    private static final String OLA_ID = "a7f3bf47-f373-4cdf-b81c-b0475305b7fe";

    @Autowired
    private UserNotificationConsumer consumer;
    @Autowired
    private NotificationRepo notificationRepo;
    @Autowired
    private UserRepo userRepo;

    private static String payload(UUID eventId, UUID attendeeId, String joinedAt) {
        return """
                {"externalEventId":"%s","eventName":"Picnic","organizerId":"%s","attendeeUserId":"%s",
                 "attendeeName":"Kari Nordmann","attendeeEmail":"kari@example.com","joinedAt":"%s"}
                """.formatted(eventId, OLA_ID, attendeeId, joinedAt);
    }

    @Test
    void aStoredNotificationIsVisibleToEveryInstanceOfTheService() {
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();

        consumer.onEventJoined(payload(eventId, attendeeId, "2026-11-18T10:15:30Z"));

        UserNotificationConsumer otherInstance = new UserNotificationConsumer(JsonMapper.builder().build(), userRepo, notificationRepo);
        assertThat(otherInstance.getNotificationsForUser(UUID.fromString(OLA_ID)))
                .filteredOn(notification -> notification.externalEventId().equals(eventId))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.message()).isEqualTo("Kari Nordmann (kari@example.com) joined your event 'Picnic'");
                    assertThat(notification.attendeeUserId()).isEqualTo(attendeeId);
                    assertThat(notification.joinedAt()).isEqualTo(Instant.parse("2026-11-18T10:15:30Z"));
                });
    }

    @Test
    void theSameMessageDeliveredTwiceIsStoredOnce() {
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();
        String message = payload(eventId, attendeeId, "2026-11-18T10:15:30Z");

        consumer.onEventJoined(message);
        consumer.onEventJoined(message);

        assertThat(consumer.getNotificationsForUser(UUID.fromString(OLA_ID)))
                .filteredOn(notification -> notification.externalEventId().equals(eventId))
                .hasSize(1);
    }

    @Test
    void joiningTheSameEventAgainLaterGivesASecondNotification() {
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();

        consumer.onEventJoined(payload(eventId, attendeeId, "2026-11-18T10:15:30Z"));
        consumer.onEventJoined(payload(eventId, attendeeId, "2026-11-18T11:00:00Z"));

        assertThat(consumer.getNotificationsForUser(UUID.fromString(OLA_ID)))
                .filteredOn(notification -> notification.externalEventId().equals(eventId))
                .extracting(UserNotificationConsumer.EventJoinedNotification::joinedAt)
                .containsExactly(Instant.parse("2026-11-18T10:15:30Z"), Instant.parse("2026-11-18T11:00:00Z"));
    }

    @Test
    void theDatabaseRejectsADuplicateEvenIfTheApplicationCheckIsSkipped() {
        UUID eventId = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();
        Instant joinedAt = Instant.parse("2026-11-18T10:15:30Z");
        notificationRepo.saveAndFlush(notification(eventId, attendeeId, joinedAt));

        assertThatThrownBy(() -> notificationRepo.saveAndFlush(notification(eventId, attendeeId, joinedAt)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void notificationsForAnOrganizerWhoIsNotAUserAreNotStored() {
        UUID vendorId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        consumer.onEventJoined("""
                {"externalEventId":"%s","eventName":"Vendor party","organizerId":"%s","attendeeUserId":"%s",
                 "attendeeName":"Kari","attendeeEmail":null,"joinedAt":"2026-11-18T10:15:30Z"}
                """.formatted(eventId, vendorId, UUID.randomUUID()));

        assertThat(consumer.getNotificationsForUser(vendorId)).isEmpty();
        assertThat(notificationRepo.findAll()).noneMatch(notification -> notification.getExternalEventId().equals(eventId));
    }

    private static Notification notification(UUID eventId, UUID attendeeId, Instant joinedAt) {
        Notification notification = new Notification();
        notification.setExternalUserId(UUID.fromString(OLA_ID));
        notification.setExternalEventId(eventId);
        notification.setEventName("Picnic");
        notification.setAttendeeUserId(attendeeId);
        notification.setMessage("Kari joined your event 'Picnic'");
        notification.setJoinedAt(joinedAt);
        return notification;
    }
}
