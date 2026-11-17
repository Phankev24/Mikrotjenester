package org.epm.event.kafka;

import java.time.Instant;
import java.util.UUID;

public record EventJoinedEvent(
        UUID externalEventId,
        String eventName,
        UUID organizerId,
        UUID attendeeUserId,
        Instant joinedAt
) {
}
