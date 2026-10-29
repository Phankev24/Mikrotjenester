package org.example.vendor.dto;

import org.example.vendor.client.EventPayload;
import org.example.vendor.vendor.Vendor;
import org.springframework.stereotype.Component;

/** All DTO -> entity/payload conversion. The service layer never sees API DTOs. */
@Component
public class VendorRequestMapper {

    /** POST: the password is stored as sent. */
    public Vendor toEntity(VendorRequest r) {
        return new Vendor(r.companyName(), r.password(), r.email(),
                r.phone(), r.website(), r.description());
    }

    /** PATCH: null means "not sent", and the service skips those fields. */
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

    /** Body sent to event's POST /api/event/vendor/{id}. The enum is sent as its name, e.g. "DINNER". */
    public EventPayload toEventPayload(VendorEventRequest r) {
        return new EventPayload(r.eventName(), r.eventDescription(),
                r.eventCategory().name(), r.eventLocation(), r.eventAttendance());
    }
}