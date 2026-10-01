package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;

import java.util.UUID;

public record EventCreateDto(
        String eventName,
        String eventDescription,
        int eventAttendance,
        EventCategory eventCategory
) {
}
