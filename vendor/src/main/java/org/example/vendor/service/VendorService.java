package org.example.vendor.service;

import org.example.vendor.client.EventClient;
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
 * Business logic. Works on entities only and identifies vendors by externalId.
 *
 * Flows:
 *  - create vendor:        email must be free, saved with eventId = null
 *  - assign to event:      event calls vendor, vendor verifies the event, then stores the link
 *  - vendor creates event: vendor asks event to create one and stores the returned id
 *  - unassign:             removes the link only, the vendor is kept
 *  - event deleted:        all vendors of that event are unlinked
 */
@Service
public class VendorService {

    private final VendorRepo vendorRepo;
    private final EventClient eventClient;

    public VendorService(VendorRepo vendorRepo, EventClient eventClient) {
        this.vendorRepo = vendorRepo;
        this.eventClient = eventClient;
    }

    /**
     * FLOW create: email must be free (409).
     * NOTE: the password is stored as plain text for now. To re-enable hashing, inject a
     * PasswordEncoder and call vendor.setPassword(passwordEncoder.encode(vendor.getPassword())).
     */
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

    /** Empty list is valid: it means the event has no vendors. */
    public List<Vendor> getVendorsByEvent(UUID eventId) {
        return vendorRepo.findByEventId(eventId);
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
     * FLOW place vendor in an existing event (called by event):
     *  1. ask the event service if the event exists (404 if not, 503 if it is down)
     *  2. load the vendor by externalId (404 if not found)
     *  3. store the eventId on the vendor
     * Idempotent. If the vendor already belonged to another event, that link is replaced.
     */
    @Transactional
    public Vendor assignToEvent(UUID externalId, UUID eventId) {
        if (!eventClient.eventExists(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + eventId);
        }
        Vendor vendor = find(externalId);
        vendor.setEventId(eventId);
        return vendorRepo.save(vendor);
    }

    /** FLOW remove vendor from its event: only the link is cleared, the vendor stays reusable. */
    @Transactional
    public Vendor unassignFromEvent(UUID externalId) {
        Vendor vendor = find(externalId);
        vendor.setEventId(null);
        return vendorRepo.save(vendor);
    }

    /** FLOW event deleted: unlink all its vendors instead of deleting vendor data. */
    @Transactional
    public void unassignAllFromEvent(UUID eventId) {
        List<Vendor> vendors = vendorRepo.findByEventId(eventId);
        vendors.forEach(v -> v.setEventId(null));
        vendorRepo.saveAll(vendors);
    }

    /**
     * FLOW vendor creates an event, knowing only its own id:
     *  1. load the vendor (404 if not found)
     *  2. refuse with 409 if it already has an event
     *  3. fill missing event fields from the company name
     *  4. the event service creates the event and returns its external UUID
     *  5. store that id on the vendor
     * KNOWN RISK: the HTTP call happens before the save. If the save fails,
     * the event exists but no vendor points to it.
     */
    @Transactional
    public Vendor createEventForVendor(UUID externalId, EventPayload payload) {
        Vendor vendor = find(externalId);
        if (vendor.getEventId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Vendor already has an event: " + vendor.getEventId());
        }
        UUID eventId = eventClient.createEvent(
                payload.withDefaults(vendor.getExternalId(), vendor.getCompanyName()));
        vendor.setEventId(eventId);
        return vendorRepo.save(vendor);
    }

    @Transactional
    public void deleteVendor(UUID externalId) {
        vendorRepo.delete(find(externalId));
    }

    // One place for "not found" so every method gives the same 404.
    private Vendor find(UUID externalId) {
        return vendorRepo.findByExternalId(externalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vendor not found: " + externalId));
    }

    // 409 if the email is taken. The unique DB constraint is the final guard against races.
    private void requireEmailFree(String email) {
        if (vendorRepo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
    }
}