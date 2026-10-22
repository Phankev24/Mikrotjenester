package org.example.vendor.client;

/** Body for event's POST /api/event/vendor/{externalVendorId} (mirrors its EventCreateDto). */
public record EventPayload(
        String eventName,
        String eventDescription,
        String eventCategory,
        String eventLocation,
        Integer eventAttendance
) {}