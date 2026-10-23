package org.epm.event.dto.vendor;

import org.epm.event.enumeration.VendorType;

import java.util.UUID;

public record VendorResponseDto(
        UUID externalId,
        UUID eventId,
        String companyName,
        String email,
        String phone,
        String website,
        String description
) {
}
