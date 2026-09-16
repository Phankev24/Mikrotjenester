package org.epm.event.dto;


import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;
import java.util.List;

public record EventWithVendorsDto(
        Long eventId,
        String eventName,
        String eventDescription,
        int eventAttendance,
        LocalDateTime eventDateTime,
        EventCategory eventCategory,
        List<VendorDto> vendors
) {
}
