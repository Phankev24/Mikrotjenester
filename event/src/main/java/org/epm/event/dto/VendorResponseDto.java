package org.epm.event.dto;

import org.epm.event.enumeration.VendorType;

public record VendorResponseDto(
        VendorType type,
        String companyName,
        String servicesDescription,
        String website,
        String phone
) {
}
