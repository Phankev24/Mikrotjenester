package org.epm.event.dto.event;

import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;

public record EventUpdateDto(
        String eventName,
        String eventDescription,
        Integer eventAttendance,
        String eventLocation,
        LocalDateTime eventDateTime,
        EventCategory eventCategory
) {
}
