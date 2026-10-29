package org.example.vendor.controller;

import jakarta.validation.Valid;
import org.example.vendor.dto.*;
import org.example.vendor.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Public API (/api/vendors/**, via gateway). Maps DTO <-> entity and delegates to the service. */
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
    public ResponseEntity<VendorResponse> createVendor(@Valid @RequestBody VendorRequest request) {
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

    /**
     * FLOW vendor posts an event: POST /api/vendors/{externalId}/events
     * Only the vendor id is needed in the URL. The event service creates the event with
     * this vendor as organizer. 404 if the vendor is unknown, 502/503 if event fails.
     */
    @PostMapping("/{externalId}/events")
    public ResponseEntity<VendorEventResponse> postEvent(@PathVariable UUID externalId,
                                                         @Valid @RequestBody VendorEventRequest request) {
        var created = vendorService.postEvent(externalId, requestMapper.toEventPayload(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toEventResponse(created));
    }

}