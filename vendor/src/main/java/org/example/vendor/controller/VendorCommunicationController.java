package org.example.vendor.controller;

import org.example.vendor.dto.VendorResponse;
import org.example.vendor.dto.VendorResponseMapper;
import org.example.vendor.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Vendor's contract with the event service. Lives under /internal, which the
 * gateway must NOT route, so only other services can reach it.
 *
 * Typical sequence from the event side:
 *  1. event creates its own event
 *  2. event calls PATCH  /internal/vendors/{vendorId}/event/{eventId}  (place vendor)
 *  3. event calls GET    /internal/vendors/event/{eventId}             (list vendors)
 *  4. event calls DELETE /internal/vendors/{vendorId}/event            (remove one vendor)
 *  5. event calls DELETE /internal/vendors/event/{eventId}             (event deleted)
 */
@RestController
@RequestMapping("/internal/vendors")
public class VendorCommunicationController {

    private final VendorService vendorService;
    private final VendorResponseMapper responseMapper;

    public VendorCommunicationController(VendorService vendorService, VendorResponseMapper responseMapper) {
        this.vendorService = vendorService;
        this.responseMapper = responseMapper;
    }

    /** Event lists its vendors. Empty list = event without vendors. */
    @GetMapping("/event/{eventId}")
    public List<VendorResponse> vendorsForEvent(@PathVariable UUID eventId) {
        return vendorService.getVendorsByEvent(eventId).stream().map(responseMapper::toResponse).toList();
    }

    /**
     * FLOW place a vendor in an existing event. PATCH because only the vendor's eventId
     * changes. Vendor verifies the event first (404 unknown event/vendor, 503 event down).
     * Call it after the event is committed, or the check will not find it.
     */
    @PatchMapping("/{externalId}/event/{eventId}")
    public VendorResponse assignToEvent(@PathVariable UUID externalId, @PathVariable UUID eventId) {
        return responseMapper.toResponse(vendorService.assignToEvent(externalId, eventId));
    }

    /** FLOW remove one vendor from its event. The vendor itself is kept. */
    @DeleteMapping("/{externalId}/event")
    public VendorResponse unassignFromEvent(@PathVariable UUID externalId) {
        return responseMapper.toResponse(vendorService.unassignFromEvent(externalId));
    }

    /** FLOW event deleted: all its vendors are unlinked (not deleted). 204 even if there were none. */
    @DeleteMapping("/event/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eventDeleted(@PathVariable UUID eventId) {
        vendorService.unassignAllFromEvent(eventId);
    }
}