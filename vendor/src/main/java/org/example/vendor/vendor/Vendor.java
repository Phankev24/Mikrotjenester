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
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private VendorType type;

    private String companyName;
    private String servicesDescription;
    private String website;
    private String phone;

    public Vendor(String phone, String website, String servicesDescription, String companyName, VendorType type, UUID id) {
        this.phone = phone;
        this.website = website;
        this.servicesDescription = servicesDescription;
        this.companyName = companyName;
        this.type = type;
        this.id = id;
    }

    @PrePersist
    public void generateUuid(){
        if(this.id == null){
            this.id = UUID.randomUUID();
        }
    }
}