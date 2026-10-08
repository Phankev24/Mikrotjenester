package org.example.vendor.dto;

import org.example.vendor.vendor.VendorType;
import java.util.UUID;

public record VendorResponse(
        Long id,
        UUID eventId,
        VendorType type,
        String companyName,
        String servicesDescription,
        String website,
        String phone
) {}
