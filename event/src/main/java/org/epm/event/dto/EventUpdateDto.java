package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;

public record EventUpdateDto(
        String eventName,
        String eventDescription,
        int eventAttendance,
        String eventLocation,
        LocalDateTime eventDateTime,
        EventCategory eventCategory
) {
}
