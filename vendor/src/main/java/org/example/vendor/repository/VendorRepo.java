package org.example.vendor.repository;

import org.example.vendor.vendor.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendorRepo extends JpaRepository<Vendor, Long> {

    // Lookup by public id. Callers never use the internal Long id.
    Optional<Vendor> findByExternalId(UUID externalId);

    boolean existsByEmail(String email);
}