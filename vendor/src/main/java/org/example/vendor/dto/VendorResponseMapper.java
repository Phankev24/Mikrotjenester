package org.example.vendor.dto;

import org.example.vendor.vendor.Vendor;
import org.springframework.stereotype.Component;

@Component
public class VendorResponseMapper {

    public VendorResponse toResponse(Vendor vendor) {
        if (vendor == null) return null;

        return new VendorResponse(
                vendor.getId(),       // Long id (sekvensielt)
                vendor.getUserId(),   // UUID userId
                vendor.getType(),
                vendor.getCompanyName(),
                vendor.getServicesDescription(),
                vendor.getWebsite(),
                vendor.getPhone()
        );
    }
}
