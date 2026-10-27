package org.epm.event.event;

import org.epm.event.enumeration.EventCategory;
import org.epm.event.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.UUID;


@Configuration
@Profile("dev")
public class Dataintializer {
    @Bean
    CommandLineRunner initDatabase(EventRepository eventRepository){
        return args -> {

            Event event1 = new Event(
                    EventCategory.CONFERENCE,
                    LocalDateTime.now().plusDays(30),
                    "Conference Hall A",
                    100,
                    "Annual Tech Conference",
                    "Tech Conference 2023",
                    UUID.randomUUID(), // eventOrganizer
                    UUID.randomUUID(), // externalVendorId
                    null, // externalUserId (null for vendor-created event)
                    UUID.randomUUID() // externalEventId
            );

            Event event2 = new Event(
                    EventCategory.SOCIAL,
                    LocalDateTime.now().plusDays(15),
                    "City Park",
                    50,
                    "Community Picnic",
                    "Summer Picnic",
                    UUID.randomUUID(), // eventOrganizer
                    null, // externalVendorId (null for user-created event)
                    UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe"), // externalUserId
                    UUID.randomUUID() // externalEventId
            );

            eventRepository.save(event1);
            eventRepository.save(event2);

            System.out.println("Sample H2 data initialized");
        };
    }
}
