package org.example.vendor.dto;

import org.example.vendor.client.EventPayload;
import org.example.vendor.vendor.Vendor;
import org.springframework.stereotype.Component;

/** All DTO -> entity/payload conversion. The service layer never sees API DTOs. */
@Component
public class VendorRequestMapper {

    /** POST: the password is still plain here; VendorService hashes it before saving. */
    public Vendor toEntity(VendorRequest r) {
        return new Vendor(r.companyName(), r.password(), r.email(),
                r.phone(), r.website(), r.description());
    }

    /** PATCH: no defaults. null means "not sent", and the service skips those fields. */
    public Vendor toPatchEntity(VendorPatchRequest r) {
        Vendor patch = new Vendor();
        patch.setCompanyName(r.companyName());
        patch.setPassword(r.password());
        patch.setEmail(r.email());
        patch.setPhone(r.phone());
        patch.setWebsite(r.website());
        patch.setDescription(r.description());
        return patch;
    }

    /** Body for creating an event. The vendor id and defaults are added by the service. */
    public EventPayload toEventPayload(VendorEventRequest r) {
        if (r == null) return new EventPayload(null, null, null, null, null, 0);
        return new EventPayload(null, r.name(), r.description(), null, r.location(),
                r.attendance() != null ? r.attendance() : 0);
    }
}