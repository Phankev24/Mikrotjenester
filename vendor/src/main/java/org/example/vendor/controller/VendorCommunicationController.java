package org.example.vendor.controller;

import org.example.vendor.dto.VendorResponse;
import org.example.vendor.dto.VendorResponseMapper;
import org.example.vendor.service.VendorService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Vendor's contract with other services. Lives under /internal, which the gateway
 * must NOT route, so only other services can reach it.
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

    /** Event looks up one vendor by externalId. Returns the vendor only. 404 if unknown. */
    @GetMapping("/{externalId}")
    public VendorResponse getVendor(@PathVariable UUID externalId) {
        return responseMapper.toResponse(vendorService.getVendor(externalId));
    }
}