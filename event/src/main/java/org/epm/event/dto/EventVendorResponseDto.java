package org.epm.event.dto;

import org.epm.event.enumeration.EventCategory;
import org.epm.event.enumeration.VendorType;

import java.util.UUID;

public record EventVendorResponseDto(
        Long eventId,
        UUID vendorId,
        String eventName,
        String eventDescription,
        int eventAttendance,
        EventCategory eventCategory,
        VendorType type,
        String companyName,
        String servicesDescription,
        String website,
        String phone
) {
}
