package org.example.vendor.config;

import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/** Dev only: fills the in-memory H2 database with sample vendors. Never runs without the "dev" profile. */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final VendorRepo vendorRepo;

    public DevDataSeeder(VendorRepo vendorRepo) {
        this.vendorRepo = vendorRepo;
    }

    @Override
    public void run(String... args) {
        if (vendorRepo.count() > 0) return;
        String pw = "password123";   // plain text, same dev password for all samples
        vendorRepo.saveAll(List.of(
                new Vendor("Oslo Catering", pw, "post@oslocatering.example", "+47 22 00 00 01",
                        "https://oslocatering.example", "Buffets and finger food for up to 200 guests."),
                new Vendor("Festlyd DJ", pw, "booking@festlyd.example", "+47 22 00 00 02",
                        "https://festlyd.example", "DJ and sound system for parties and company events."),
                new Vendor("Blomsten Blomster", pw, "hei@blomsten.example", "+47 22 00 00 03",
                        "https://blomsten.example", "Flowers and table decorations."),
                new Vendor("Lys og Tone", pw, "info@lysogtone.example", "+47 22 00 00 04",
                        "https://lysogtone.example", "Lighting and stage rental.")
        ));
    }
}