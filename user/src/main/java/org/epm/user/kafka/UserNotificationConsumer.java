package org.epm.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.epm.user.notification.Notification;
import org.epm.user.notification.NotificationRepo;
import org.epm.user.user.User;
import org.epm.user.user.UserRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationConsumer {

    public static final String EVENT_JOINED_TOPIC = "event-joined";

    private final ObjectMapper objectMapper;
    private final UserRepo userRepo;
    private final NotificationRepo notificationRepo;

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

    @KafkaListener(topics = EVENT_JOINED_TOPIC, groupId = "user-notification-${HOSTNAME:${COMPUTERNAME:local}}")
    public void onEventJoined(String payload) {
        EventJoinedEvent event;
        try {
            event = objectMapper.readValue(payload, EventJoinedEvent.class);
        } catch (JacksonException e) {
            log.error("Dropped unreadable event-joined payload: {}", payload, e);
            return;
        }
        log.info("Received event-joined message for event {}", event.externalEventId());

        userRepo.findByExternalUserId(event.organizerId()).ifPresentOrElse(
                organizer -> store(organizer, event),
                () -> log.info("Ignored event-joined message for event {}, organizer {} is not a user",
                        event.externalEventId(), event.organizerId()));
    }

    public List<EventJoinedNotification> getNotificationsForUser(UUID externalUserId) {
        return notificationRepo.findByExternalUserIdOrderByJoinedAtAscIdAsc(externalUserId).stream()
                .map(notification -> new EventJoinedNotification(
                        notification.getExternalEventId(),
                        notification.getEventName(),
                        notification.getAttendeeUserId(),
                        notification.getMessage(),
                        notification.getJoinedAt()))
                .toList();
    }

    private void store(User organizer, EventJoinedEvent event) {
        if (notificationRepo.existsByExternalEventIdAndAttendeeUserIdAndJoinedAt(
                event.externalEventId(), event.attendeeUserId(), event.joinedAt())) {
            log.info("Ignored duplicate event-joined message for event {}", event.externalEventId());
            return;
        }

        String attendeeDisplay = (event.attendeeEmail() != null && !event.attendeeEmail().isBlank())
                ? String.format("%s (%s)", event.attendeeName(), event.attendeeEmail())
                : event.attendeeName();
        String message = String.format("%s joined your event '%s'", attendeeDisplay, event.eventName());

        Notification notification = new Notification();
        notification.setExternalUserId(organizer.getExternalUserId());
        notification.setExternalEventId(event.externalEventId());
        notification.setEventName(event.eventName());
        notification.setAttendeeUserId(event.attendeeUserId());
        notification.setMessage(message);
        notification.setJoinedAt(event.joinedAt() != null ? event.joinedAt() : Instant.now());

        try {
            notificationRepo.save(notification);
        } catch (DataIntegrityViolationException e) {
            log.info("Ignored duplicate event-joined message for event {}", event.externalEventId());
            return;
        }
        log.info("Notification stored for user organizer {} ({}): {}",
                organizer.getUsername(), organizer.getExternalUserId(), message);
    }
}
