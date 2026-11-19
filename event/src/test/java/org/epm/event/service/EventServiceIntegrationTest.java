package org.epm.event.service;

import org.epm.event.IntegrationTestBase;
import org.epm.event.TestData;
import org.epm.event.event.EventAttendance;
import org.epm.event.repository.EventAttendanceRepository;
import org.epm.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private EventService eventService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventAttendanceRepository eventAttendanceRepository;

    @Test
    void deletingAnEventAlsoRemovesTheRegistrationsOfItsAttendees() {
        UUID organizer = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        eventRepository.save(TestData.event(eventId, organizer, organizer, null, "Event with attendees", 10));
        eventAttendanceRepository.save(new EventAttendance(eventId, UUID.randomUUID()));
        eventAttendanceRepository.save(new EventAttendance(eventId, UUID.randomUUID()));

        eventService.deleteEvent(eventId);

        assertThat(eventRepository.findByExternalEventId(eventId)).isEmpty();
        assertThat(eventAttendanceRepository.countByExternalEventId(eventId)).isZero();
    }

    @Test
    void deletingAnEventWithoutAttendeesWorks() {
        UUID organizer = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        eventRepository.save(TestData.event(eventId, organizer, organizer, null, "Empty event", 10));

        eventService.deleteEvent(eventId);

        assertThat(eventRepository.findByExternalEventId(eventId)).isEmpty();
    }

    @Test
    void deletingAnUnknownEventGives404() {
        assertThatThrownBy(() -> eventService.deleteEvent(UUID.randomUUID()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
