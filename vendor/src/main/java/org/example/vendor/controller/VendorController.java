package org.example.vendor.controller;

import org.example.vendor.dto.*;
import org.example.vendor.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Public API (/api/vendors/**, via gateway). Maps DTO <-> entity, delegates to the service. */
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

    /** Creates a vendor without an event. Body is optional (defaults are filled in). */
    @PostMapping
    public ResponseEntity<VendorResponse> createVendor(@RequestBody(required = false) VendorRequest request) {
        var saved = vendorService.createVendor(requestMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(saved));
    }

    @GetMapping
    public List<VendorResponse> getAllVendors() {
        return vendorService.getAllVendors().stream().map(responseMapper::toResponse).toList();
    }

    @GetMapping("/{externalId}")
    public VendorResponse getVendor(@PathVariable UUID externalId) {
        return responseMapper.toResponse(vendorService.getVendor(externalId));
    }

    /** Vendor creates an event; only the vendor id is needed. The response contains the new eventId. */
    @PostMapping("/{externalId}/events")
    public ResponseEntity<VendorResponse> createEvent(@PathVariable UUID externalId,
                                                      @RequestBody(required = false) VendorEventRequest request) {
        var updated = vendorService.createEventForVendor(externalId, requestMapper.toEventPayload(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(updated));
    }

    /** PATCH: only the sent fields change. */
    @PatchMapping("/{externalId}")
    public VendorResponse updateVendor(@PathVariable UUID externalId, @RequestBody VendorRequest request) {
        return responseMapper.toResponse(
                vendorService.updateVendor(externalId, requestMapper.toPatchEntity(request)));
    }

    @DeleteMapping("/{externalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVendor(@PathVariable UUID externalId) {
        vendorService.deleteVendor(externalId);
    }
}