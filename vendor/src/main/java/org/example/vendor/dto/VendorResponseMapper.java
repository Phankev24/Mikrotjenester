package org.example.vendor.dto;

import org.example.vendor.client.EventDto;
import org.example.vendor.vendor.Vendor;
import org.springframework.stereotype.Component;

/** Entity/client DTO -> API response. The only place that decides which fields leave the service. */
@Component
public class VendorResponseMapper {

    public VendorResponse toResponse(Vendor vendor) {
        if (vendor == null) return null;
        return new VendorResponse(
                vendor.getExternalId(),
                vendor.getCompanyName(),
                vendor.getEmail(),
                vendor.getPhone(),
                vendor.getWebsite(),
                vendor.getDescription());
    }

    public VendorEventResponse toEventResponse(EventDto e) {
        if (e == null) return null;
        return new VendorEventResponse(
                e.externalEventId(),
                e.externalVendorId(),
                e.eventName(),
                e.eventDescription(),
                e.eventCategory(),
                e.eventLocation(),
                e.eventDateTime(),
                e.eventAttendance());
    }
}