package org.example.vendor.config;

import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Dev only: fills the in-memory H2 database with sample vendors. Never runs without the "dev" profile.
 * The UUIDs are fixed, so they are the same after every restart. That keeps Swagger requests,
 * saved test calls, and events in the event service (which store these ids) valid.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    // Fixed test ids. Easy to type, and clearly fake.
    public static final UUID OSLO_CATERING = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID FESTLYD_DJ    = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID BLOMSTEN      = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final UUID LYS_OG_TONE   = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private final VendorRepo vendorRepo;

    public DevDataSeeder(VendorRepo vendorRepo) {
        this.vendorRepo = vendorRepo;
    }

    @Override
    public void run(String... args) {
        if (vendorRepo.count() > 0) return;
        String pw = "password123";   // plain text, same dev password for all samples
        vendorRepo.saveAll(List.of(
                new Vendor(OSLO_CATERING, "Oslo Catering", pw, "post@oslocatering.example", "+47 22 00 00 01",
                        "https://oslocatering.example", "Buffets and finger food for up to 200 guests."),
                new Vendor(FESTLYD_DJ, "Festlyd DJ", pw, "booking@festlyd.example", "+47 22 00 00 02",
                        "https://festlyd.example", "DJ and sound system for parties and company events."),
                new Vendor(BLOMSTEN, "Blomsten Blomster", pw, "hei@blomsten.example", "+47 22 00 00 03",
                        "https://blomsten.example", "Flowers and table decorations."),
                new Vendor(LYS_OG_TONE, "Lys og Tone", pw, "info@lysogtone.example", "+47 22 00 00 04",
                        "https://lysogtone.example", "Lighting and stage rental.")
        ));
    }
}