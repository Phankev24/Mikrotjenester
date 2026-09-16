package org.epm.event.dto;

import org.epm.event.enumeration.VendorType;

import java.util.UUID;

public record VendorDto(
        UUID id,
        UUID userId,
        VendorType type,
        String companyName,
        String servicesDescription,
        String website,
        String phone
){}
