package org.example.vendor.dto;

import org.example.vendor.vendor.Vendor;
import org.example.vendor.vendor.VendorType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VendorRequestMapper {

    /** POST: fyller inn default-verdier der felter mangler (request kan være null/tom). */
    public Vendor toEntity(UUID eventId, VendorRequest request) {
        return new Vendor(
                eventId,
                request != null && request.type() != null ? request.type() : VendorType.CATERER,
                request != null && request.companyName() != null ? request.companyName() : "Default Company",
                request != null && request.servicesDescription() != null ? request.servicesDescription() : "Default Description",
                request != null && request.website() != null ? request.website() : "https://default.vendor",
                request != null && request.phone() != null ? request.phone() : "+47 000 000"
        );
    }

    /** PATCH: ingen defaults. Felter som ikke er sendt inn blir null. */
    public Vendor toPatchEntity(VendorRequest request) {
        Vendor patch = new Vendor();
        if (request == null) return patch;
        patch.setType(request.type());
        patch.setCompanyName(request.companyName());
        patch.setServicesDescription(request.servicesDescription());
        patch.setWebsite(request.website());
        patch.setPhone(request.phone());
        return patch;
    }
}