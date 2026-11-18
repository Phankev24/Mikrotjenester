package org.example.vendor.controller;

import jakarta.validation.Valid;
import org.example.vendor.dto.GoogleLoginRequest;
import org.example.vendor.dto.LoginRequest;
import org.example.vendor.dto.LoginResponse;
import org.example.vendor.service.VendorService;
import org.example.vendor.vendor.Vendor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final VendorService vendorService;

    public AuthController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Vendor vendor = vendorService.login(request.email(), request.password());
        return new LoginResponse(vendor.getExternalId(), vendor.getCompanyName(), vendor.getEmail());
    }

    @PostMapping("/google")
    public LoginResponse google(@Valid @RequestBody GoogleLoginRequest request) {
        Vendor vendor = vendorService.loginWithGoogle(request.idToken());
        return new LoginResponse(vendor.getExternalId(), vendor.getCompanyName(), vendor.getEmail());
    }
}