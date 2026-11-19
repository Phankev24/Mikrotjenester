package org.epm.event;

import org.epm.event.dto.user.UserResponseDto;
import org.epm.event.dto.vendor.VendorResponseDto;
import org.epm.event.enumeration.EventCategory;
import org.epm.event.event.Event;

import java.time.LocalDateTime;
import java.util.UUID;

public final class TestData {

    private TestData() {
    }

    public static Event event(UUID externalEventId, UUID organizer, UUID userId, UUID vendorId, String name, Integer capacity) {
        return new Event(EventCategory.SOCIAL, LocalDateTime.of(2027, 1, 1, 18, 0), "Oslo", capacity,
                "A description", name, organizer, vendorId, userId, externalEventId);
    }

    public static UserResponseDto user(UUID externalUserId, String firstName) {
        return new UserResponseDto(1L, externalUserId, firstName.toLowerCase(), firstName, "Nordmann",
                firstName.toLowerCase() + "@example.com", "12345678", 30, "Male");
    }

    public static VendorResponseDto vendor(UUID externalId, String companyName) {
        return new VendorResponseDto(externalId, null, companyName, "vendor@example.com", "87654321",
                "https://example.com", "A vendor");
    }
}
