package org.example.vendor.dto;

import java.util.UUID;

/** What the API returns for a vendor. No password and no internal id. */
public record VendorResponse(
        UUID externalId,
        String companyName,
        String email,
        String phone,
        String website,
        String description
) {}