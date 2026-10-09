package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;

import java.util.UUID;


public record EventResponseDto(
        Long eventId,
        UUID externalEventId,
        UUID externalUserId,
        UUID externalVendorId,
        String eventName,
        String eventDescription,
        int eventAttendance,
        String eventLocation,
        EventCategory eventCategory
){}
