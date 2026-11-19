package org.example.vendor.dto;

import org.example.vendor.client.EventDto;
import org.example.vendor.client.EventPayload;
import org.example.vendor.vendor.Vendor;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VendorMappersTest {

    final VendorRequestMapper requestMapper = new VendorRequestMapper();
    final VendorResponseMapper responseMapper = new VendorResponseMapper();

    // ---------- VendorRequestMapper ----------

    @Test
    void toEntity_copiesAllFields_andLeavesExternalIdForPrePersist() {
        Vendor v = requestMapper.toEntity(new VendorRequest(
                "Firma", "secret123", "a@b.no", "111", "https://x.no", "Beskrivelse"));

        assertThat(v.getCompanyName()).isEqualTo("Firma");
        assertThat(v.getPassword()).isEqualTo("secret123");   // hashing happens in the service
        assertThat(v.getEmail()).isEqualTo("a@b.no");
        assertThat(v.getPhone()).isEqualTo("111");
        assertThat(v.getWebsite()).isEqualTo("https://x.no");
        assertThat(v.getDescription()).isEqualTo("Beskrivelse");
        assertThat(v.getExternalId()).isNull();
    }

    @Test
    void toPatchEntity_missingFieldsStayNull() {
        Vendor patch = requestMapper.toPatchEntity(new VendorPatchRequest(null, null, null, "999", null, null));

        assertThat(patch.getPhone()).isEqualTo("999");
        assertThat(patch.getCompanyName()).isNull();
        assertThat(patch.getEmail()).isNull();
        assertThat(patch.getPassword()).isNull();
        assertThat(patch.getWebsite()).isNull();
        assertThat(patch.getDescription()).isNull();
    }

    // ---------- VendorResponseMapper ----------

    @Test
    void toResponse_mapsPublicFields() {
        UUID id = UUID.randomUUID();
        Vendor v = new Vendor(id, "Firma", "HASH", "a@b.no", "111", "https://x.no", "Beskrivelse");

        VendorResponse r = responseMapper.toResponse(v);

        assertThat(r.externalId()).isEqualTo(id);
        assertThat(r.companyName()).isEqualTo("Firma");
        assertThat(r.email()).isEqualTo("a@b.no");
        assertThat(r.phone()).isEqualTo("111");
        assertThat(r.website()).isEqualTo("https://x.no");
        assertThat(r.description()).isEqualTo("Beskrivelse");
    }

    @Test
    void toResponse_hasNoPasswordComponent() {
        assertThat(VendorResponse.class.getRecordComponents())
                .extracting(c -> c.getName())
                .doesNotContain("password", "id");
    }

    @Test
    void toResponse_and_toDTO_giveSameResult() {
        Vendor v = new Vendor(UUID.randomUUID(), "Firma", "HASH", "a@b.no", null, null, null);

        assertThat(responseMapper.toResponse(v)).isEqualTo(responseMapper.toDTO(v));
    }

    @Test
    void toResponse_null_returnsNull() {
        assertThat(responseMapper.toResponse(null)).isNull();
        assertThat(responseMapper.toDTO(null)).isNull();
    }

    @Test
    void toEventResponse_mapsAllFields() {
        UUID eventId = UUID.randomUUID();
        UUID vendorId = UUID.randomUUID();
        LocalDateTime when = LocalDateTime.of(2026, 11, 1, 18, 0);

        VendorEventResponse r = responseMapper.toEventResponse(
                new EventDto(eventId, vendorId, "Fest", "Beskrivelse", "DINNER", "Oslo", when, 50));

        assertThat(r.externalEventId()).isEqualTo(eventId);
        assertThat(r.externalVendorId()).isEqualTo(vendorId);
        assertThat(r.eventName()).isEqualTo("Fest");
        assertThat(r.eventCategory()).isEqualTo("DINNER");
        assertThat(r.eventLocation()).isEqualTo("Oslo");
        assertThat(r.eventDateTime()).isEqualTo(when);
        assertThat(r.eventAttendance()).isEqualTo(50);
    }

    @Test
    void toEventResponse_null_returnsNull() {
        assertThat(responseMapper.toEventResponse(null)).isNull();
    }
}