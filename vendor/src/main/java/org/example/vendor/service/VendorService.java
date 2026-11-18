package org.example.vendor.service;

import org.example.vendor.client.EventClient;
import org.example.vendor.client.EventDto;
import org.example.vendor.client.EventPayload;
import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.example.vendor.security.GoogleTokenVerifier;

import java.util.List;
import java.util.UUID;

@Service
public class VendorService {

    private final VendorRepo vendorRepo;
    private final EventClient eventClient;
    private final PasswordEncoder passwordEncoder;

    public VendorService(
            VendorRepo vendorRepo,
            EventClient eventClient,
            PasswordEncoder passwordEncoder,
            GoogleTokenVerifier googleVerifier) {

        this.vendorRepo = vendorRepo;
        this.eventClient = eventClient;
        this.passwordEncoder = passwordEncoder;
        this.googleVerifier = googleVerifier;
    }

    @Transactional
    public Vendor createVendor(Vendor vendor) {

        requireEmailFree(vendor.getEmail());

        vendor.setPassword(
                passwordEncoder.encode(
                        vendor.getPassword()));

        return vendorRepo.save(vendor);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepo.findAll();
    }

    public Vendor getVendor(UUID externalId) {
        return find(externalId);
    }

    @Transactional
    public Vendor updateVendor(UUID externalId, Vendor patch) {

        Vendor vendor = find(externalId);

        if (patch.getCompanyName() != null) {
            vendor.setCompanyName(patch.getCompanyName());
        }

        if (patch.getEmail() != null &&
                !patch.getEmail().equals(vendor.getEmail())) {

            requireEmailFree(patch.getEmail());

            vendor.setEmail(patch.getEmail());
        }

        if (patch.getPassword() != null) {
            vendor.setPassword(
                    passwordEncoder.encode(
                            patch.getPassword()));
        }

        if (patch.getPhone() != null) {
            vendor.setPhone(patch.getPhone());
        }

        if (patch.getWebsite() != null) {
            vendor.setWebsite(patch.getWebsite());
        }

        if (patch.getDescription() != null) {
            vendor.setDescription(patch.getDescription());
        }

        return vendorRepo.save(vendor);
    }

    public Vendor login(
            String email,
            String password) {

        Vendor vendor = vendorRepo.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid credentials"));

        boolean valid =
                passwordEncoder.matches(
                        password,
                        vendor.getPassword());

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials");
        }

        return vendor;
    }

    public EventDto postEvent(
            UUID vendorExternalId,
            EventPayload payload) {

        find(vendorExternalId);

        return eventClient.createVendorEvent(
                vendorExternalId,
                payload);
    }

    @Transactional
    public void deleteVendor(UUID externalId) {
        vendorRepo.delete(find(externalId));
    }

    private Vendor find(UUID externalId) {

        return vendorRepo.findByExternalId(externalId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Vendor not found: " + externalId));
    }

    private void requireEmailFree(String email) {

        if (vendorRepo.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered");
        }
    }

    private final GoogleTokenVerifier googleVerifier;

// Add method:
    /**
     * Google login: verified email -> existing vendor, or a new one.
     * New accounts get a random hashed password, so password login is impossible until it is changed.
     */
    @Transactional
    public Vendor loginWithGoogle(String idToken) {
        GoogleTokenVerifier.GoogleUser g = googleVerifier.verify(idToken);
        return vendorRepo.findByEmail(g.email()).orElseGet(() -> vendorRepo.save(new Vendor(
                g.name() != null && !g.name().isBlank() ? g.name() : g.email(),
                passwordEncoder.encode(UUID.randomUUID().toString()),
                g.email(), null, null, null)));
    }
}