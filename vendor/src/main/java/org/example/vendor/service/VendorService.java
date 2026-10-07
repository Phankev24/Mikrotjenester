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

    public VendorResponse getById(Long id) {
        Vendor vendor = vendorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return toResponse(vendor);
    }

    public VendorResponse updateVendor(Long id, VendorRequest request) {
        Vendor vendor = vendorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (request.type() != null) vendor.setType(request.type());
        if (request.companyName() != null) vendor.setCompanyName(request.companyName());
        if (request.servicesDescription() != null) vendor.setServicesDescription(request.servicesDescription());
        if (request.website() != null) vendor.setWebsite(request.website());
        if (request.phone() != null) vendor.setPhone(request.phone());

        vendorRepo.save(vendor);
        return toResponse(vendor);
    }

    public void deleteVendor(Long id) {
        if (!vendorRepo.existsById(id)) {
            throw new RuntimeException("Vendor not found");
        }
        vendorRepo.deleteById(id);
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
