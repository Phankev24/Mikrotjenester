package org.example.vendor.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for "vendor posts an event". The vendor id comes from the URL.
 * Mirrors event's EventCreateDto. eventCategory must be a value of event's EventCategory enum.
 */
public record VendorEventRequest(
        @NotBlank String eventName,
        String eventDescription,
        @NotBlank String eventCategory,
        String eventLocation,
        Integer eventAttendance
) {}