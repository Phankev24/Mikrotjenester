package org.example.vendor.dto;

import org.example.vendor.vendor.VendorType;

public record VendorRequest(
        VendorType type,
        String companyName,
        String servicesDescription,
        String website,
        String phone
) {}