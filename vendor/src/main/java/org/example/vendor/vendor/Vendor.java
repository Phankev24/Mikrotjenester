package org.example.vendor.vendor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vendor")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // sekvensiell PK

    @Column(nullable = false)
    private UUID eventId;           // UUID fra Event-tjenesten

    @Enumerated(EnumType.STRING)
    private VendorType type;

    private String companyName;
    private String servicesDescription;
    private String website;
    private String phone;

    public Vendor(UUID eventId,
                  VendorType type,
                  String companyName,
                  String servicesDescription,
                  String website,
                  String phone) {
        this.eventId = eventId;
        this.type = type;
        this.companyName = companyName;
        this.servicesDescription = servicesDescription;
        this.website = website;
        this.phone = phone;
    }
}
