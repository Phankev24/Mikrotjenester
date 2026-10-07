package org.example.vendor.controller;

import org.example.vendor.dto.VendorRequest;
import org.example.vendor.dto.VendorResponse;
import org.example.vendor.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Forvent at klient sender x-user-id i header (bruk ekte auth senere)
    @PostMapping
    public ResponseEntity<VendorResponse> createVendor(
            @RequestHeader("x-user-id") UUID userId,
            @RequestBody VendorRequest request) {
        VendorResponse createdVendor = vendorService.createVendor(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVendor);
    }

    @GetMapping
    public List<VendorResponse> getAllVendors() {
        return vendorService.getAllVendors();
    }

    @GetMapping("/{id}")
    public VendorResponse getVendor(@PathVariable Long id) {
        return vendorService.getById(id);
    }

    @PatchMapping("/{id}")
    public VendorResponse updateVendor(
            @PathVariable Long id,
            @RequestBody VendorRequest request
    ) {
        return vendorService.updateVendor(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
    }
}
