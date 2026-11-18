package org.epm.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.epm.user.user.UserRepo;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationConsumer {

    private final ObjectMapper objectMapper;
    private final UserRepo userRepo;
    private final Map<UUID, List<EventJoinedNotification>> notificationsByUser = new ConcurrentHashMap<>();

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

    @KafkaListener(topics = "event-joined", groupId = "user-notification-group")
    public void onEventJoined(String payload) {
        try {
            EventJoinedEvent event = objectMapper.readValue(payload, EventJoinedEvent.class);

            userRepo.findByExternalUserId(event.organizerId()).ifPresent(organizer -> {
                String attendeeDisplay = (event.attendeeEmail() != null && !event.attendeeEmail().isBlank())
                        ? String.format("%s (%s)", event.attendeeName(), event.attendeeEmail())
                        : event.attendeeName();

                String msg = String.format(
                        "%s joined your event '%s'",
                        attendeeDisplay,
                        event.eventName()
                );

                notificationsByUser
                        .computeIfAbsent(organizer.getExternalUserId(), k -> new CopyOnWriteArrayList<>())
                        .add(new EventJoinedNotification(
                                event.externalEventId(),
                                event.eventName(),
                                event.attendeeUserId(),
                                msg,
                                event.joinedAt()
                        ));
                log.info("Notification stored for user organizer {} ({}): {}",
                        organizer.getUsername(), organizer.getExternalUserId(), msg);
            });
        } catch (Exception e) {
            log.error("Failed to consume event-joined payload: {}", payload, e);
        }
    }

    public List<EventJoinedNotification> getNotificationsForUser(UUID externalUserId) {
        return notificationsByUser.getOrDefault(externalUserId, List.of());
    }
}