package org.example.vendor.repository;

import org.example.vendor.vendor.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepo extends JpaRepository<Vendor, Long> {

    // Vendors placed in one event. Empty list = event without vendors.
    List<Vendor> findByEventId(UUID eventId);

    // Lookup by public id. Callers never use the internal Long id.
    Optional<Vendor> findByExternalId(UUID externalId);

    boolean existsByEmail(String email);
}