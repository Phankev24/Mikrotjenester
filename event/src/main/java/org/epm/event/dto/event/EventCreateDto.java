package org.epm.event.dto.event;

import org.epm.event.enumeration.EventCategory;


public record EventCreateDto(
        String eventName,
        String eventDescription,
        EventCategory eventCategory,
        String eventLocation,
        Integer eventAttendance

) {
}
