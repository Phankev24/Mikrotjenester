package org.example.vendor.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vendor.repository.VendorRepo;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorNotificationConsumer {
    private final ObjectMapper objectMapper;
    private final VendorRepo vendorRepo;
    private final Map<UUID, List<EventJoinedNotification>> notificationsByVendor = new ConcurrentHashMap<>();

    public record EventJoinedEvent(
            UUID externalEventId,
            String eventName,
            UUID organizerId,
            UUID attendeeUserId,
            String attendeeName,
            String attendeeEmail,
            Instant joinedAt
    ) {}

    public record EventJoinedNotification(
            UUID externalEventId,
            String eventName,
            UUID attendeeUserId,
            String message,
            Instant joinedAt
    ) {}

    @KafkaListener(topics = "event-joined", groupId = "vendor-notification-group")
    public void onEventJoined(String payload) {
        try {
            EventJoinedEvent event = objectMapper.readValue(payload, EventJoinedEvent.class);

            vendorRepo.findByExternalId(event.organizerId()).ifPresent(organizer -> {
                String attendeeDisplay = (event.attendeeEmail() != null && !event.attendeeEmail().isBlank())
                        ? String.format("%s (%s)", event.attendeeName(), event.attendeeEmail())
                        : event.attendeeName();

                String msg = String.format(
                        "%s joined your event '%s'",
                        attendeeDisplay,
                        event.eventName()
                );

                notificationsByVendor
                        .computeIfAbsent(organizer.getExternalId(), k -> new CopyOnWriteArrayList<>())
                        .add(new EventJoinedNotification(
                                event.externalEventId(),
                                event.eventName(),
                                event.attendeeUserId(),
                                msg,
                                event.joinedAt()
                        ));
                log.info("Notification stored for vendor organizer {} ({}): {}",
                        organizer.getCompanyName(), organizer.getExternalId(), msg);
            });
        } catch (Exception e) {
            log.error("Failed to consume event-joined payload: {}", payload, e);
        }
    }

    public List<EventJoinedNotification> getNotificationsForVendor(UUID externalVendorId) {
        return notificationsByVendor.getOrDefault(externalVendorId, List.of());
    }
}