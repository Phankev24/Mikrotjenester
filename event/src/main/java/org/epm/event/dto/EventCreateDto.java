package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;

import java.util.UUID;


public record EventCreateDto(
        UUID externalVendorId,
        String eventName,
        String eventDescription,
        EventCategory eventCategory,
        String eventLocation,
        int eventAttendance

) {
}
