package org.example.vendor.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** A vendor event as the API returns it. Event's internal Long id is deliberately left out. */
public record VendorEventResponse(
        UUID externalEventId,
        UUID externalVendorId,
        String eventName,
        String eventDescription,
        String eventCategory,
        String eventLocation,
        LocalDateTime eventDateTime,
        Integer eventAttendance
) {}