package org.example.vendor.config;

import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.example.vendor.vendor.VendorType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.UUID;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedVendors(VendorRepo vendorRepo) {
        return args -> {
            vendorRepo.save(new Vendor(
                    UUID.randomUUID(),
                    VendorType.CATERER,
                    "Oslo Catering",
                    "Buffet and vegan options",
                    "https://oslocatering.no",
                    "+47 999 888"
            ));
            vendorRepo.save(new Vendor(
                    UUID.randomUUID(),
                    VendorType.PHOTOGRAPHER,
                    "Nordic Lens",
                    "Wedding and event photography",
                    "https://nordiclens.no",
                    "+47 111 222"
            ));
        };
    }
}