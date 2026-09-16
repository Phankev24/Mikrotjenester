package org.example.vendor.controller;

import org.example.vendor.dto.VendorRequest;
import org.example.vendor.dto.VendorResponse;
import org.example.vendor.service.VendorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping
    public VendorResponse createVendor(
            @RequestHeader("x-user-id") UUID userId,
            @RequestBody VendorRequest request
    ) {
        return vendorService.createVendor(userId, request);
    }

    @GetMapping
    public List<VendorResponse> getAllVendors() {
        return vendorService.getAllVendors();
    }
}