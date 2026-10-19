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
 * gateway must NOT route, so only other services can call it.
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

    /** Event puts a vendor into an event. PUT because repeating it gives the same result. */
    @PutMapping("/{externalId}/event/{eventId}")
    public VendorResponse assignToEvent(@PathVariable UUID externalId, @PathVariable UUID eventId) {
        return responseMapper.toResponse(vendorService.assignToEvent(externalId, eventId));
    }

    /** Event removes one vendor from itself. The vendor is kept. */
    @DeleteMapping("/{externalId}/event")
    public VendorResponse unassignFromEvent(@PathVariable UUID externalId) {
        return responseMapper.toResponse(vendorService.unassignFromEvent(externalId));
    }

    /** Event was deleted: its vendors are unlinked, not deleted. */
    @DeleteMapping("/event/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eventDeleted(@PathVariable UUID eventId) {
        vendorService.unassignAllFromEvent(eventId);
    }
}