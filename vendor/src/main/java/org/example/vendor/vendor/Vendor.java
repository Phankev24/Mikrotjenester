package org.example.vendor.vendor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Vendor entity. Knows only an eventId, never a user (User -> Event -> Vendor). */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vendor")
public class Vendor {

    // Internal database key. Never exposed through the API.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    // Public id used by clients and other services. Set once, never changes.
    @Column(nullable = false, unique = true, updatable = false)
    @Setter(AccessLevel.NONE)
    private UUID externalId;

    // Event in the event service. null = not linked. A plain value, not a foreign key.
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    private VendorType type;

    private String companyName;
    private String servicesDescription;
    private String website;
    private String phone;

    // A new vendor starts without an event; linking happens later.
    public Vendor(VendorType type, String companyName, String servicesDescription,
                  String website, String phone) {
        this.type = type;
        this.companyName = companyName;
        this.servicesDescription = servicesDescription;
        this.website = website;
        this.phone = phone;
    }

    @PrePersist
    void generateExternalId() {
        if (externalId == null) externalId = UUID.randomUUID();
    }
}