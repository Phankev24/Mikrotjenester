package org.example.vendor.dto;

import org.example.vendor.vendor.VendorType;
import java.util.UUID;

public record VendorResponse(
        Long id,
        UUID userId,
        VendorType type,
        String companyName,
        String servicesDescription,
        String website,
        String phone
) {}
