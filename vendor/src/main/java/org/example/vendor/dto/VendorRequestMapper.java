package org.example.vendor.dto;

import org.example.vendor.client.EventPayload;
import org.example.vendor.vendor.Vendor;
import org.example.vendor.vendor.VendorType;
import org.springframework.stereotype.Component;

/** All DTO -> entity/payload conversion. Services never see API DTOs. */
@Component
public class VendorRequestMapper {

    /** POST: missing fields get defaults, so an empty body works (handy in Swagger). */
    public Vendor toEntity(VendorRequest request) {
        VendorRequest r = request != null ? request : new VendorRequest(null, null, null, null, null);
        return new Vendor(
                orDefault(r.type(), VendorType.CATERER),
                orDefault(r.companyName(), "Default Company"),
                orDefault(r.servicesDescription(), "Default Description"),
                orDefault(r.website(), "https://default.vendor"),
                orDefault(r.phone(), "+47 000 000"));
    }

    /** PATCH: no defaults. null means "do not change", which the service relies on. */
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

    /** Body for creating an event. The vendor id is added later by the service. */
    public EventPayload toEventPayload(VendorEventRequest r) {
        if (r == null) {
            return new EventPayload(null, null, null, null, null, 0);
        }
        return new EventPayload(
                null,
                r.name(),
                r.description(),
                null,
                r.location(),
                r.attendance() != null ? r.attendance() : 0
        );
    }

    private static <T> T orDefault(T value, T fallback) {
        return value != null ? value : fallback;
    }
}