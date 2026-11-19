package org.example.vendor.kafka;

import org.example.vendor.notification.Notification;
import org.example.vendor.notification.NotificationRepo;
import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorNotificationConsumerTest {

    private static final UUID VENDOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EVENT_ID = UUID.fromString("3f2c1b7a-0000-4000-8000-000000000001");
    private static final UUID ATTENDEE_ID = UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90");
    private static final Instant JOINED_AT = Instant.parse("2026-11-18T10:15:30Z");

    @Mock
    private VendorRepo vendorRepo;
    @Mock
    private NotificationRepo notificationRepo;

    private VendorNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new VendorNotificationConsumer(JsonMapper.builder().build(), vendorRepo, notificationRepo);
    }

    private static String payload(UUID organizerId, String attendeeEmail) {
        return """
                {"externalEventId":"%s","eventName":"Summer buffet","organizerId":"%s",
                 "attendeeUserId":"%s","attendeeName":"Kari Nordmann","attendeeEmail":%s,
                 "joinedAt":"2026-11-18T10:15:30Z"}
                """.formatted(EVENT_ID, organizerId, ATTENDEE_ID,
                attendeeEmail == null ? "null" : "\"" + attendeeEmail + "\"");
    }

    private void organizerIsAVendor() {
        when(vendorRepo.findByExternalId(VENDOR_ID)).thenReturn(Optional.of(
                new Vendor(VENDOR_ID, "Oslo Catering", "HASH", "post@oslo.no", "111", "https://oslo.no", "Mat")));
    }

    @Test
    void storesANotificationWithNameAndEmailForTheVendor() {
        organizerIsAVendor();

        consumer.onEventJoined(payload(VENDOR_ID, "kari@example.com"));

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(saved.capture());
        Notification notification = saved.getValue();
        assertThat(notification.getMessage()).isEqualTo("Kari Nordmann (kari@example.com) joined your event 'Summer buffet'");
        assertThat(notification.getExternalVendorId()).isEqualTo(VENDOR_ID);
        assertThat(notification.getExternalEventId()).isEqualTo(EVENT_ID);
        assertThat(notification.getAttendeeUserId()).isEqualTo(ATTENDEE_ID);
        assertThat(notification.getJoinedAt()).isEqualTo(JOINED_AT);
    }

    @Test
    void leavesOutTheEmailWhenItIsMissing() {
        organizerIsAVendor();

        consumer.onEventJoined(payload(VENDOR_ID, null));

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(saved.capture());
        assertThat(saved.getValue().getMessage()).isEqualTo("Kari Nordmann joined your event 'Summer buffet'");
    }

    @Test
    void ignoresEventsWhoseOrganizerIsNotAVendor() {
        UUID userOrganizer = UUID.randomUUID();
        when(vendorRepo.findByExternalId(userOrganizer)).thenReturn(Optional.empty());

        consumer.onEventJoined(payload(userOrganizer, "kari@example.com"));

        verify(notificationRepo, never()).save(any());
    }

    @Test
    void dropsAMalformedPayloadInsteadOfCrashingTheListener() {
        consumer.onEventJoined("this is not json");

        verify(notificationRepo, never()).save(any());
    }

    @Test
    void doesNotStoreTheSameJoinTwice() {
        organizerIsAVendor();
        when(notificationRepo.existsByExternalEventIdAndAttendeeUserIdAndJoinedAt(EVENT_ID, ATTENDEE_ID, JOINED_AT))
                .thenReturn(true);

        consumer.onEventJoined(payload(VENDOR_ID, "kari@example.com"));

        verify(notificationRepo, never()).save(any());
    }

    @Test
    void ignoresADuplicateThatSlipsThroughTheCheck() {
        organizerIsAVendor();
        when(notificationRepo.save(any(Notification.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        consumer.onEventJoined(payload(VENDOR_ID, "kari@example.com"));

        verify(notificationRepo).save(any(Notification.class));
    }

    @Test
    void letsDatabaseFailuresPropagateSoKafkaRedeliversTheMessage() {
        organizerIsAVendor();
        when(notificationRepo.save(any(Notification.class))).thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> consumer.onEventJoined(payload(VENDOR_ID, "kari@example.com")))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void returnsTheStoredNotificationsOfAVendor() {
        Notification stored = new Notification();
        stored.setExternalVendorId(VENDOR_ID);
        stored.setExternalEventId(EVENT_ID);
        stored.setEventName("Summer buffet");
        stored.setAttendeeUserId(ATTENDEE_ID);
        stored.setMessage("Kari Nordmann joined your event 'Summer buffet'");
        stored.setJoinedAt(JOINED_AT);
        when(notificationRepo.findByExternalVendorIdOrderByJoinedAtAscIdAsc(VENDOR_ID)).thenReturn(List.of(stored));

        assertThat(consumer.getNotificationsForVendor(VENDOR_ID)).singleElement().satisfies(notification -> {
            assertThat(notification.externalEventId()).isEqualTo(EVENT_ID);
            assertThat(notification.message()).isEqualTo("Kari Nordmann joined your event 'Summer buffet'");
            assertThat(notification.joinedAt()).isEqualTo(JOINED_AT);
        });
    }

    @Test
    void returnsAnEmptyListForVendorsWithoutNotifications() {
        assertThat(consumer.getNotificationsForVendor(UUID.randomUUID())).isEmpty();
    }
}
