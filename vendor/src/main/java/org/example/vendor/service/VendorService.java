package org.example.vendor.service;

import org.example.vendor.dto.VendorRequestMapper;
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
    private final VendorRequestMapper vendorRequestMapper;

    public VendorService(VendorRepo vendorRepo, VendorRequestMapper vendorRequestMapper) {
        this.vendorRepo = vendorRepo;
        this.vendorRequestMapper = vendorRequestMapper;
    }

    public VendorRequest createVendor(VendorRequest request) {
        Vendor vendorEntity = vendorRequestMapper.toEntity(request);
        Vendor savedVendor = vendorRepo.save(vendorEntity);
        return vendorRequestMapper.toDTO(savedVendor);
    }

    public List<VendorResponse> getAllVendors() {
        return vendorRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VendorResponse updateVendor(UUID vendorId, VendorRequest request) {
        Vendor vendor = vendorRepo.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (request.type() != null) vendor.setType(request.type());
        if (request.companyName() != null) vendor.setCompanyName(request.companyName());
        if (request.servicesDescription() != null) vendor.setServicesDescription(request.servicesDescription());
        if (request.website() != null) vendor.setWebsite(request.website());
        if (request.phone() != null) vendor.setPhone(request.phone());

        vendorRepo.save(vendor);
        return toResponse(vendor);
    }

    public void deleteVendor(UUID vendorId) {
        if (!vendorRepo.existsById(vendorId)) {
            throw new RuntimeException("Vendor not found");
        }
        vendorRepo.deleteById(vendorId);
    }

    private VendorResponse toResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getUserId(),
                vendor.getId(),
                vendor.getType(),
                vendor.getCompanyName(),
                vendor.getServicesDescription(),
                vendor.getWebsite(),
                vendor.getPhone()
        );
    }
}
