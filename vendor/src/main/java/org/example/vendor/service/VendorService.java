package org.example.vendor.service;

import org.example.vendor.client.EventClient;
import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class VendorService {

    private final VendorRepo vendorRepo;
    private final EventClient eventClient;

    public VendorService(VendorRepo vendorRepo, EventClient eventClient) {
        this.vendorRepo = vendorRepo;
        this.eventClient = eventClient;
    }

    @Transactional
    public Vendor createVendor(Vendor vendor, boolean validateEvent) {
        if (validateEvent && !eventClient.eventExists(vendor.getEventId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Event not found: " + vendor.getEventId());
        }
        return vendorRepo.save(vendor);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepo.findAll();
    }

    public List<Vendor> getVendorsByEvent(UUID eventId) {
        return vendorRepo.findByEventId(eventId);
    }

    @Transactional
    public Vendor updateVendor(Long id, Vendor patch) {
        Vendor vendor = vendorRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found: " + id));

        if (patch.getType() != null) vendor.setType(patch.getType());
        if (patch.getCompanyName() != null) vendor.setCompanyName(patch.getCompanyName());
        if (patch.getServicesDescription() != null) vendor.setServicesDescription(patch.getServicesDescription());
        if (patch.getWebsite() != null) vendor.setWebsite(patch.getWebsite());
        if (patch.getPhone() != null) vendor.setPhone(patch.getPhone());

        return vendorRepo.save(vendor);
    }

    @Transactional
    public void deleteVendor(Long id) {
        if (!vendorRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found: " + id);
        }
        vendorRepo.deleteById(id);
    }
}