package org.epm.user.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventDto(
        Long internalEventId,
        UUID externalEventId,
        UUID externalUserId,
        UUID externalVendorId,
        String eventName,
        String eventDescription,
        EventCategory eventCategory,
        String eventLocation,
        LocalDateTime eventDateTime,
        int eventAttendance
) {
}
