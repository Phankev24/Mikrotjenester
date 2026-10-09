package org.epm.event.event;

import org.epm.event.enumeration.EventCategory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;


@Configuration
@Profile("dev")
public class Dataintializer {
    @Bean
    CommandLineRunner initDatabase(EventRepository eventRepository){
        return args -> {
            Event event1 = new Event();
            event1.setEventName("Bowling med jobb");
            event1.setEventDescription("Bowling med kollegaer og masse morro");
            event1.setEventAttendance(10);
            event1.setEventLocation("Bowling 1 - Torggata 16, 0181 OSLO");
            event1.setEventDateTime(LocalDateTime.now());
            event1.setEventCategory(EventCategory.SOCIAL);

            eventRepository.save(event1);

            System.out.println("Sample H2 data initialized");
        };
    }
}
