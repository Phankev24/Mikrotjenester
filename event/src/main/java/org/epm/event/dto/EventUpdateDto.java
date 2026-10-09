package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;

public record EventUpdateDto(
        String eventName,
        String eventDescription,
        EventCategory eventCategory,
        String eventLocation,
        LocalDateTime eventDateTime,
        int eventAttendance
) {
}
