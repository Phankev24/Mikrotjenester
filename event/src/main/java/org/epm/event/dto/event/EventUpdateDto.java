package org.epm.event.dto.event;

import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;

public record EventUpdateDto(
        String eventOrganizer,
        String eventName,
        String eventDescription,
        EventCategory eventCategory,
        String eventLocation,
        LocalDateTime eventDateTime,
        Integer eventAttendance
) {
}
