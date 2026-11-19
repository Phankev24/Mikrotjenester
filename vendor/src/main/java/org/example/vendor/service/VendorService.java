package org.example.vendor.service;

import lombok.extern.slf4j.Slf4j;
import org.example.vendor.client.EventClient;
import org.example.vendor.client.EventDto;
import org.example.vendor.client.EventPayload;
import org.example.vendor.repository.VendorRepo;
import org.example.vendor.security.GoogleTokenVerifier;
import org.example.vendor.vendor.Vendor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class VendorService {

    private final VendorRepo vendorRepo;
    private final EventClient eventClient;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleVerifier;

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

        Vendor saved = vendorRepo.save(vendor);
        log.info("Vendor created: {} ({})", vendor.getCompanyName(), vendor.getExternalId());
        return saved;
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

        Vendor saved = vendorRepo.save(vendor);
        log.info("Vendor updated: {}", externalId);
        return saved;
    }

    public Vendor login(String email, String password) {
        Vendor vendor = vendorRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Vendor login failed, unknown email {}", email);
                    return new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid credentials");
                });

        boolean valid = passwordEncoder.matches(password, vendor.getPassword());

        if (!valid) {
            log.warn("Vendor login failed, wrong password for {}", email);
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials");
        }

        log.info("Vendor logged in: {}", vendor.getExternalId());
        return vendor;
    }

    /**
     * Google login: verified email -> existing vendor, or a new one.
     * New accounts get a random hashed password, so password login is impossible until it is changed.
     */
    @Transactional
    public Vendor loginWithGoogle(String idToken) {
        GoogleTokenVerifier.GoogleUser g = googleVerifier.verify(idToken);
        return vendorRepo.findByEmail(g.email()).orElseGet(() -> {
            log.info("Creating vendor from Google login: {}", g.email());
            return vendorRepo.save(new Vendor(
                    g.name() != null && !g.name().isBlank() ? g.name() : g.email(),
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    g.email(), null, null, null));
        });
    }

    public EventDto postEvent(UUID vendorExternalId, EventPayload payload) {
        find(vendorExternalId);
        log.info("Vendor {} posts an event to the event service", vendorExternalId);
        return eventClient.createVendorEvent(vendorExternalId, payload);
    }

    @Transactional
    public void deleteVendor(UUID externalId) {
        vendorRepo.delete(find(externalId));
        log.info("Vendor deleted: {}", externalId);
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
}