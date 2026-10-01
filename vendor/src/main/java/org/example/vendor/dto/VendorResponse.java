package org.example.vendor.dto;

import org.example.vendor.vendor.VendorType;
import java.util.UUID;

public record VendorResponse(
        Long userId,
        UUID id,
        VendorType type,
        String companyName,
        String servicesDescription,
        String website,
        String phone
) {}