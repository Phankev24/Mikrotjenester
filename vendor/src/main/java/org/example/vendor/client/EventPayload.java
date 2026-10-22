package org.example.vendor.client;

import java.util.UUID;

/**
 * Body for event's POST /api/event (mirrors its EventCreateDto).
 * externalVendorId is the vendor's own externalId, so event stores its side of the link.
 */
public record EventPayload(
        UUID externalVendorId,
        String eventName,
        String eventDescription,
        String eventCategory,
        String eventLocation,
        int eventAttendance
) {
    /** A vendor can create an event knowing only its own id: gaps are filled from the vendor. */
    public EventPayload withDefaults(UUID vendorId, String vendorName) {
        return new EventPayload(
                vendorId,
                eventName != null ? eventName : "Event by " + vendorName,
                eventDescription != null ? eventDescription : "Created by vendor " + vendorName,
                eventCategory != null ? eventCategory : "SOCIAL",   // the only category I have seen
                eventLocation != null ? eventLocation : "To be decided",
                eventAttendance);
    }
}