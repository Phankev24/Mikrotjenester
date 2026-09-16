package org.example.vendor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.vendor.vendor.Vendor;
import java.util.UUID;

public interface VendorRepo extends JpaRepository<Vendor, UUID> {
}