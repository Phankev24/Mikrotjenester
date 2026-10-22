package org.example.vendor.service;

import org.example.vendor.client.EventClient;
import org.example.vendor.client.EventDto;
import org.example.vendor.client.EventPayload;
import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Business logic. Works on entities and client DTOs, never API DTOs.
 * Vendors are identified by externalId. Events are owned by the event service.
 */
@Service
public class VendorService {

    private final VendorRepo vendorRepo;
    private final EventClient eventClient;

    public VendorService(VendorRepo vendorRepo, EventClient eventClient) {
        this.vendorRepo = vendorRepo;
        this.eventClient = eventClient;
    }

    /** FLOW create: email must be free (409). Password is stored as plain text for now. */
    @Transactional
    public Vendor createVendor(Vendor vendor) {
        requireEmailFree(vendor.getEmail());
        return vendorRepo.save(vendor);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepo.findAll();
    }

    public Vendor getVendor(UUID externalId) {
        return find(externalId);
    }

    /** FLOW patch: only non-null fields in "patch" are applied. A new email must be free. */
    @Transactional
    public Vendor updateVendor(UUID externalId, Vendor patch) {
        Vendor vendor = find(externalId);
        if (patch.getCompanyName() != null) vendor.setCompanyName(patch.getCompanyName());
        if (patch.getEmail() != null && !patch.getEmail().equals(vendor.getEmail())) {
            requireEmailFree(patch.getEmail());
            vendor.setEmail(patch.getEmail());
        }
        if (patch.getPassword() != null) vendor.setPassword(patch.getPassword());
        if (patch.getPhone() != null) vendor.setPhone(patch.getPhone());
        if (patch.getWebsite() != null) vendor.setWebsite(patch.getWebsite());
        if (patch.getDescription() != null) vendor.setDescription(patch.getDescription());
        return vendorRepo.save(vendor);
    }

    /**
     * FLOW vendor posts an event, knowing only its own id:
     *  1. check the vendor exists (404)
     *  2. event service creates the event with this vendor as organizer
     * Nothing is saved locally, so there is no half-finished state to clean up.
     * Deliberately not @Transactional: no database work, and we should not hold a
     * connection open during an HTTP call.
     */
    public EventDto postEvent(UUID vendorExternalId, EventPayload payload) {
        find(vendorExternalId);
        return eventClient.createVendorEvent(vendorExternalId, payload);
    }

    /** FLOW list a vendor's events (read from the event service). */
    public List<EventDto> getEvents(UUID vendorExternalId) {
        find(vendorExternalId);
        return eventClient.getEventsByVendor(vendorExternalId);
    }

    /** Deleting a vendor does not touch its events; event keeps them with the old vendor id. */
    @Transactional
    public void deleteVendor(UUID externalId) {
        vendorRepo.delete(find(externalId));
    }

    private Vendor find(UUID externalId) {
        return vendorRepo.findByExternalId(externalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vendor not found: " + externalId));
    }

    private void requireEmailFree(String email) {
        if (vendorRepo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
    }
}