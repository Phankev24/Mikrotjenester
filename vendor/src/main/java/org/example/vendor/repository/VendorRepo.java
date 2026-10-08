package org.example.vendor.repository;

import org.example.vendor.vendor.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VendorRepo extends JpaRepository<Vendor, Long> {

    List<Vendor> findByEventId(UUID eventId);
}