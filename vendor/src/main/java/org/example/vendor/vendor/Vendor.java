package org.example.vendor.vendor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Vendor {

    @Id
    private UUID id;

    private UUID userId;

    @Enumerated(EnumType.STRING)
    private VendorType type;

    private String companyName;
    private String servicesDescription;
    private String website;
    private String phone;

    public Vendor(UUID userId, VendorType type, String companyName,
                  String servicesDescription, String website, String phone) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.companyName = companyName;
        this.servicesDescription = servicesDescription;
        this.website = website;
        this.phone = phone;
    }
}