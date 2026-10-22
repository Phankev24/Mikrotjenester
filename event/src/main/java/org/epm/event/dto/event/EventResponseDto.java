package org.epm.event.dto.event;

import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;
import java.util.UUID;


public record EventResponseDto(
        Long internalEventId,
        UUID externalEventId,
        UUID externalUserId,
        UUID externalVendorId,
        String eventOrganizer,
        String eventName,
        String eventDescription,
        EventCategory eventCategory,
        String eventLocation,
        LocalDateTime eventDateTime,
        Integer eventAttendance
        ){}
