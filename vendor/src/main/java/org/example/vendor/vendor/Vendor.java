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
    private Long id;                // sekvensielt id (DB generert)

    @Column(name = "user_id", nullable = false)
    private UUID userId;            // UUID for eier/bruker

    @Enumerated(EnumType.STRING)
    private VendorType type;

    private String companyName;
    private String servicesDescription;
    private String website;
    private String phone;

    public Vendor(UUID userId,
                  VendorType type,
                  String companyName,
                  String servicesDescription,
                  String website,
                  String phone) {
        this.userId = userId;
        this.type = type;
        this.companyName = companyName;
        this.servicesDescription = servicesDescription;
        this.website = website;
        this.phone = phone;
    }
}
