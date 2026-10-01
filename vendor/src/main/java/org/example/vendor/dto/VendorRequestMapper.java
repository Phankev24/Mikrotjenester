package org.example.vendor.dto;

import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.springframework.stereotype.Component;

@Component
public class VendorRequestMapper {
    private final VendorRepo vendorRepo;

    public VendorRequestMapper(VendorRepo vendorRepo) {
        this.vendorRepo = vendorRepo;
    }

    public VendorRequest toDTO(Vendor vendor){
        if(vendor == null) return null;

        return new VendorRequest(
                vendor.getType(),
                vendor.getCompanyName(),
                vendor.getWebsite(),
                vendor.getServicesDescription(),
                vendor.getPhone()
        );
    }

    public Vendor toEntity(VendorRequest vendorRequest){
        if(vendorRequest == null) return null;

        Vendor vendor = new Vendor();
        vendor.setCompanyName(vendorRequest.companyName());
        vendor.setType(vendorRequest.type());
        vendor.setPhone(vendorRequest.phone());
        vendor.setServicesDescription(vendorRequest.servicesDescription());
        vendor.setWebsite(vendorRequest.website());
        return vendor;
    }
}
