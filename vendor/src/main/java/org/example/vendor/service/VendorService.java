package org.example.vendor.service;

import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.example.vendor.dto.VendorRequest;
import org.example.vendor.dto.VendorResponse;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class VendorService {

    private final VendorRepo vendorRepo;

    public VendorService(VendorRepo vendorRepo) {
        this.vendorRepo = vendorRepo;
    }

    public VendorResponse createVendor(UUID userId, VendorRequest request) {
        Vendor vendor = new Vendor(
                userId,
                request.type(),
                request.companyName(),
                request.servicesDescription(),
                request.website(),
                request.phone()
        );

        vendorRepo.save(vendor);

        return toResponse(vendor);
    }

    public List<VendorResponse> getAllVendors() {
        return vendorRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VendorResponse toResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getUserId(),
                vendor.getType(),
                vendor.getCompanyName(),
                vendor.getServicesDescription(),
                vendor.getWebsite(),
                vendor.getPhone()
        );
    }
}