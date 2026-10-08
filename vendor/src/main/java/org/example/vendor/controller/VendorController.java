package org.example.vendor.controller;

import org.example.vendor.dto.*;
import org.example.vendor.service.VendorService;
import org.example.vendor.vendor.Vendor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PostMapping
    public ResponseEntity<VendorResponse> createVendor(
            @RequestHeader(value = "x-event-id", required = false) UUID eventId,
            @RequestBody(required = false) VendorRequest request) {

        boolean eventProvided = eventId != null;
        UUID effectiveEventId = eventProvided ? eventId : UUID.randomUUID();

        Vendor saved = vendorService.createVendor(
                requestMapper.toEntity(effectiveEventId, request),
                eventProvided);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(saved));
    }

    @GetMapping
    public List<VendorResponse> getAllVendors() {
        return vendorService.getAllVendors().stream().map(responseMapper::toResponse).toList();
    }

    @GetMapping("/event/{eventId}")
    public List<VendorResponse> getVendorsByEvent(@PathVariable UUID eventId) {
        return vendorService.getVendorsByEvent(eventId).stream().map(responseMapper::toResponse).toList();
    }

    @PatchMapping("/{id}")
    public VendorResponse updateVendor(@PathVariable Long id,
                                       @RequestBody VendorRequest request) {
        Vendor updated = vendorService.updateVendor(id, requestMapper.toPatchEntity(request));
        return responseMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
    }
}