package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;

import java.util.UUID;

public record EventVendorCreateDto(
        String eventName,
        String eventDescription,
        UUID vendorId,
        int eventAttendance,
        EventCategory eventCategory
) {
}
