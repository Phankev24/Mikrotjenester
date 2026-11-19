package org.epm.event.repository;

import org.epm.event.IntegrationTestBase;
import org.epm.event.TestData;
import org.epm.event.event.Event;
import org.epm.event.event.EventAttendance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventRepositoryTest extends IntegrationTestBase {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventAttendanceRepository eventAttendanceRepository;

    @Test
    @Transactional
    void findsEventsByOrganizerUserVendorAndExternalId() {
        UUID organizer = UUID.randomUUID();
        UUID vendor = UUID.randomUUID();
        Event userEvent = eventRepository.save(TestData.event(UUID.randomUUID(), organizer, organizer, null, "User event", 10));
        Event vendorEvent = eventRepository.save(TestData.event(UUID.randomUUID(), vendor, null, vendor, "Vendor event", 10));

        assertThat(eventRepository.findByEventOrganizer(organizer)).containsExactly(userEvent);
        assertThat(eventRepository.findByExternalUserId(organizer)).containsExactly(userEvent);
        assertThat(eventRepository.findByExternalVendorId(vendor)).containsExactly(vendorEvent);
        assertThat(eventRepository.findByExternalEventId(vendorEvent.getExternalEventId())).contains(vendorEvent);
    }

    @Test
    @Transactional
    void lockedLookupFindsTheEvent() {
        Event event = eventRepository.save(TestData.event(UUID.randomUUID(), UUID.randomUUID(), null, null, "Locked", 10));

        assertThat(eventRepository.findByExternalEventIdForUpdate(event.getExternalEventId())).contains(event);
    }

    @Test
    @Transactional
    void attendanceQueriesCountAndDeleteByEvent() {
        UUID eventId = UUID.randomUUID();
        UUID ola = UUID.randomUUID();
        UUID kari = UUID.randomUUID();
        eventAttendanceRepository.save(new EventAttendance(eventId, ola));
        eventAttendanceRepository.save(new EventAttendance(eventId, kari));

        assertThat(eventAttendanceRepository.countByExternalEventId(eventId)).isEqualTo(2);
        assertThat(eventAttendanceRepository.existsByExternalEventIdAndExternalUserId(eventId, ola)).isTrue();
        assertThat(eventAttendanceRepository.findByExternalUserId(kari)).hasSize(1);
        assertThat(eventAttendanceRepository.findByExternalEventIdAndExternalUserId(eventId, kari)).isPresent();

        eventAttendanceRepository.deleteByExternalEventId(eventId);

        assertThat(eventAttendanceRepository.countByExternalEventId(eventId)).isZero();
    }

    @Test
    void theSameUserCannotJoinTheSameEventTwice() {
        UUID eventId = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        eventAttendanceRepository.saveAndFlush(new EventAttendance(eventId, user));

        assertThatThrownBy(() -> eventAttendanceRepository.saveAndFlush(new EventAttendance(eventId, user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
