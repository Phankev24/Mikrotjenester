package org.example.vendor.service;

import lombok.extern.slf4j.Slf4j;
import org.example.vendor.dto.LoginRequest;
import org.example.vendor.dto.LoginResponse;
import org.example.vendor.dto.VendorResponse;
import org.example.vendor.dto.VendorResponseMapper;
import org.example.vendor.vendor.Vendor;
import org.example.vendor.repository.VendorRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final VendorRepo vendorRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final VendorResponseMapper vendorResponseMapper;

    public AuthService(VendorRepo vendorRepo, PasswordEncoder passwordEncoder,
                       TokenService tokenService, VendorResponseMapper vendorResponseMapper) {
        this.vendorRepo = vendorRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.vendorResponseMapper = vendorResponseMapper;
    }

    public LoginResponse login(LoginRequest request) {
        Vendor vendor = vendorRepo.findByEmail(request.email())
                .filter(v -> passwordEncoder.matches(request.password(), v.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Vendor login failed for {}", request.email());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
                });

        String token = tokenService.issueToken(vendor, "LOCAL");
        log.info("Vendor logged in: {}", vendor.getExternalId());
        return new LoginResponse(vendor.getExternalId(), vendor.getCompanyName(), vendor.getEmail(), token);
    }

    public VendorResponse meFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("Vendor profile requested without a bearer token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        return me(tokenService.subjectOf(authorization.substring("Bearer ".length())));
    }

    public VendorResponse me(UUID externalId) {
        return vendorRepo.findByExternalId(externalId)
                .map(vendorResponseMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Vendor profile requested for unknown vendor {}", externalId);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown vendor");
                });
    }
}