package org.example.vendor.dto;

import org.example.vendor.vendor.Vendor;
import org.springframework.stereotype.Component;

/** Entity -> API response. The only place that decides which fields leave the service. */
@Component
public class VendorResponseMapper {

    public VendorResponse toResponse(Vendor vendor) {
        if (vendor == null) return null;
        return new VendorResponse(
                vendor.getExternalId(),
                vendor.getEventId(),
                vendor.getCompanyName(),
                vendor.getEmail(),
                vendor.getPhone(),
                vendor.getWebsite(),
                vendor.getDescription());
    }
}