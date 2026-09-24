package org.example.vendor.dto;

import java.util.List;
import java.util.UUID;

public record VendorFullDto(
        UUID vendorId,
        String companyName,
        String vendorType,
        String servicesDescription,
        String website,
        String phone,

        // Events this vendor is participating in (optional)
        List<Long> participatingEventIds,

        // Events this vendor is hosting (optional)
        List<Long> hostedEventIds,

        // Users who interacted with this vendor (optional)
        List<UUID> relatedUserIds
) {}
