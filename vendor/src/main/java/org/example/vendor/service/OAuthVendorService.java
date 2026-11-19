package org.example.vendor.service;

import org.example.vendor.vendor.Vendor;
import org.example.vendor.repository.VendorRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class OAuthVendorService {

    private final VendorRepo vendorRepo;
    private final PasswordEncoder passwordEncoder;

    public OAuthVendorService(VendorRepo vendorRepo, PasswordEncoder passwordEncoder) {
        this.vendorRepo = vendorRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Vendor findOrCreate(String provider, OAuth2User principal) {
        return switch (provider) {
            case "github" -> findOrCreateOAuthVendor(principal);
            case "google" -> findOrCreateOAuthVendor(principal);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + provider);
        };
    }

    private Vendor findOrCreateOAuthVendor(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String login = principal.getAttribute("login");

        String companyName = (name != null && !name.isBlank()) ? name : (login != null ? login : email);

        return vendorRepo.findByEmail(email).orElseGet(() -> {
            String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
            Vendor vendor = new Vendor(companyName, encodedPassword, email, null, null, null);
            return vendorRepo.save(vendor);
        });
    }
}