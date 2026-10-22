package org.example.vendor.dto;

import java.util.UUID;

/** What the API returns. No password and no internal id. eventId is null when not in an event. */
public record VendorResponse(
        UUID externalId,
        UUID eventId,
        String companyName,
        String email,
        String phone,
        String website,
        String description
) {}