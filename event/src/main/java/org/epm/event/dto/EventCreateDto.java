package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;

import java.util.UUID;


public record EventCreateDto(
        String eventName,
        UUID vendorId,
        String eventDescription,
        int eventAttendance,
        String eventLocation,
        EventCategory eventCategory
) {
}
