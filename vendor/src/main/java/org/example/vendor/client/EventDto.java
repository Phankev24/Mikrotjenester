package org.example.vendor.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

/** The parts of event's EventResponseDto that vendor uses. Other fields (including internalEventId) are ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventDto(
        UUID externalEventId,
        UUID externalVendorId,
        String eventName,
        String eventDescription,
        String eventCategory,
        String eventLocation,
        LocalDateTime eventDateTime,
        Integer eventAttendance
) {}