package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;


public record EventResponseDto(
        Long eventId,
        String eventName,
        String eventDescription,
        int eventAttendance,
        EventCategory eventCategory
){}
