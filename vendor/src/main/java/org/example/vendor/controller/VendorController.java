package org.example.vendor.controller;

import jakarta.validation.Valid;
import org.example.vendor.dto.*;
import org.example.vendor.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public API (/api/vendors/**, reached through the gateway).
 * Maps DTO <-> entity and delegates to the service.
 * Placing a vendor in an existing event is NOT here: event does that through
 * VendorCommunicationController.
 */
@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;
    private final VendorRequestMapper requestMapper;
    private final VendorResponseMapper responseMapper;

    public VendorController(VendorService vendorService,
                            VendorRequestMapper requestMapper,
                            VendorResponseMapper responseMapper) {
        this.vendorService = vendorService;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    /** FLOW create vendor: body is required and validated. Starts without an event. */
    @PostMapping
    public ResponseEntity<VendorResponse> createVendor(@Valid @RequestBody VendorRequest request) {
        var saved = vendorService.createVendor(requestMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(saved));
    }

    @GetMapping
    public List<VendorResponse> getAllVendors() {
        return vendorService.getAllVendors().stream().map(responseMapper::toResponse).toList();
    }

    // The path uses the public externalId, never the internal database id.
    @GetMapping("/{externalId}")
    public VendorResponse getVendor(@PathVariable UUID externalId) {
        return responseMapper.toResponse(vendorService.getVendor(externalId));
    }

    /**
     * FLOW vendor creates event: POST /api/vendors/{externalId}/events
     * Only the vendor id is required (body optional). The event service creates the
     * event, and the response shows the vendor with its new eventId.
     * 409 if the vendor already has an event, 404 if the vendor does not exist.
     */
    @PostMapping("/{externalId}/events")
    public ResponseEntity<VendorResponse> createEvent(@PathVariable UUID externalId,
                                                      @RequestBody(required = false) VendorEventRequest request) {
        var updated = vendorService.createEventForVendor(externalId, requestMapper.toEventPayload(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(updated));
    }

    /** PATCH: only the sent fields change. */
    @PatchMapping("/{externalId}")
    public VendorResponse updateVendor(@PathVariable UUID externalId,
                                       @Valid @RequestBody VendorPatchRequest request) {
        return responseMapper.toResponse(
                vendorService.updateVendor(externalId, requestMapper.toPatchEntity(request)));
    }

    @DeleteMapping("/{externalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVendor(@PathVariable UUID externalId) {
        vendorService.deleteVendor(externalId);
    }
}