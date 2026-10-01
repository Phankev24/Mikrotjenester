package org.example.vendor.controller;

import org.example.vendor.dto.VendorRequest;
import org.example.vendor.dto.VendorResponse;
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

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping
    public ResponseEntity<VendorRequest> createVendor(@RequestBody VendorRequest request){
        VendorRequest createdVendor = vendorService.createVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVendor);
    }







    @GetMapping
    public List<VendorResponse> getAllVendors() {
        return vendorService.getAllVendors();
    }

    // PATCH — update vendor
    @PatchMapping("/{vendorId}")
    public VendorResponse updateVendor(
            @PathVariable UUID vendorId,
            @RequestBody VendorRequest request
    ) {
        return vendorService.updateVendor(vendorId, request);
    }

    // DELETE — remove vendor
    @DeleteMapping("/{vendorId}")
    public void deleteVendor(@PathVariable UUID vendorId) {
        vendorService.deleteVendor(vendorId);
    }
}
