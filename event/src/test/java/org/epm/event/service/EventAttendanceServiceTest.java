package org.epm.event.service;

import org.epm.event.TestData;
import org.epm.event.client.EventClient;
import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;
import org.epm.event.dto.user.EventAttendeeOverviewDto;
import org.epm.event.dto.user.EventOrganizerOverviewDto;
import org.epm.event.dto.vendor.EventVendorOverviewDto;
import org.epm.event.event.Event;
import org.epm.event.event.EventAttendance;
import org.epm.event.kafka.EventJoinedEvent;
import org.epm.event.kafka.EventKafkaProducer;
import org.epm.event.repository.EventAttendanceRepository;
import org.epm.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventAttendanceServiceTest {

    private static final UUID ORGANIZER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID KARI_ID = UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90");
    private static final UUID VENDOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EVENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventAttendanceRepository eventAttendanceRepository;
    @Mock
    private EventClient eventClient;
    @Mock
    private EventKafkaProducer eventKafkaProducer;

    private EventAttendanceService service;

    @BeforeEach
    void setUp() {
        service = new EventAttendanceService(eventRepository, eventAttendanceRepository, eventClient, eventKafkaProducer);
    }

    private static Event event(Integer capacity) {
        return TestData.event(EVENT_ID, ORGANIZER_ID, ORGANIZER_ID, null, "Summer Picnic", capacity);
    }

    private static void assertStatus(Throwable thrown, HttpStatus expected) {
        assertThat(thrown).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    @Test
    void registerAttendanceSavesTheRegistrationAndPublishesAKafkaMessage() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(TestData.user(KARI_ID, "Kari"));
        when(eventRepository.findByExternalEventIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event(2)));
        when(eventAttendanceRepository.existsByExternalEventIdAndExternalUserId(EVENT_ID, KARI_ID)).thenReturn(false);
        when(eventAttendanceRepository.countByExternalEventId(EVENT_ID)).thenReturn(1);

        service.registerAttendance(EVENT_ID, KARI_ID);

        ArgumentCaptor<EventAttendance> saved = ArgumentCaptor.forClass(EventAttendance.class);
        verify(eventAttendanceRepository).save(saved.capture());
        assertThat(saved.getValue().getExternalEventId()).isEqualTo(EVENT_ID);
        assertThat(saved.getValue().getExternalUserId()).isEqualTo(KARI_ID);

        ArgumentCaptor<EventJoinedEvent> message = ArgumentCaptor.forClass(EventJoinedEvent.class);
        verify(eventKafkaProducer).publishEventJoined(message.capture());
        assertThat(message.getValue().eventName()).isEqualTo("Summer Picnic");
        assertThat(message.getValue().organizerId()).isEqualTo(ORGANIZER_ID);
        assertThat(message.getValue().attendeeUserId()).isEqualTo(KARI_ID);
        assertThat(message.getValue().attendeeName()).isEqualTo("Kari Nordmann");
        assertThat(message.getValue().attendeeEmail()).isEqualTo("kari@example.com");
        assertThat(message.getValue().joinedAt()).isNotNull();
    }

    @Test
    void registerAttendanceThrows404WhenTheUserIsUnknown() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(null);

        assertStatus(catchThrowable(() -> service.registerAttendance(EVENT_ID, KARI_ID)), HttpStatus.NOT_FOUND);
        verify(eventAttendanceRepository, never()).save(any());
    }

    @Test
    void registerAttendanceThrows404WhenTheEventIsUnknown() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(TestData.user(KARI_ID, "Kari"));
        when(eventRepository.findByExternalEventIdForUpdate(EVENT_ID)).thenReturn(Optional.empty());

        assertStatus(catchThrowable(() -> service.registerAttendance(EVENT_ID, KARI_ID)), HttpStatus.NOT_FOUND);
    }

    @Test
    void registerAttendanceThrows409WhenAlreadyRegistered() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(TestData.user(KARI_ID, "Kari"));
        when(eventRepository.findByExternalEventIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event(2)));
        when(eventAttendanceRepository.existsByExternalEventIdAndExternalUserId(EVENT_ID, KARI_ID)).thenReturn(true);

        assertStatus(catchThrowable(() -> service.registerAttendance(EVENT_ID, KARI_ID)), HttpStatus.CONFLICT);
        verify(eventKafkaProducer, never()).publishEventJoined(any());
    }

    @Test
    void registerAttendanceThrows400WhenTheEventIsFull() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(TestData.user(KARI_ID, "Kari"));
        when(eventRepository.findByExternalEventIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event(2)));
        when(eventAttendanceRepository.existsByExternalEventIdAndExternalUserId(EVENT_ID, KARI_ID)).thenReturn(false);
        when(eventAttendanceRepository.countByExternalEventId(EVENT_ID)).thenReturn(2);

        assertStatus(catchThrowable(() -> service.registerAttendance(EVENT_ID, KARI_ID)), HttpStatus.BAD_REQUEST);
        verify(eventAttendanceRepository, never()).save(any());
    }

    @Test
    void registerAttendanceTreatsAMissingCapacityAsZero() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(TestData.user(KARI_ID, "Kari"));
        when(eventRepository.findByExternalEventIdForUpdate(EVENT_ID)).thenReturn(Optional.of(event(null)));
        when(eventAttendanceRepository.existsByExternalEventIdAndExternalUserId(EVENT_ID, KARI_ID)).thenReturn(false);
        when(eventAttendanceRepository.countByExternalEventId(EVENT_ID)).thenReturn(0);

        assertStatus(catchThrowable(() -> service.registerAttendance(EVENT_ID, KARI_ID)), HttpStatus.BAD_REQUEST);
    }

    @Test
    void getEventAttendeesCountsRegistrationsAndSkipsUnknownUsers() {
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.of(event(10)));
        when(eventAttendanceRepository.findByExternalEventId(EVENT_ID))
                .thenReturn(List.of(new EventAttendance(EVENT_ID, KARI_ID), new EventAttendance(EVENT_ID, ORGANIZER_ID)));
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(TestData.user(KARI_ID, "Kari"));
        when(eventClient.getUserByExternalId(ORGANIZER_ID)).thenReturn(null);

        EventAttendanceResponseDto result = service.getEventAttendees(EVENT_ID);

        assertThat(result.eventName()).isEqualTo("Summer Picnic");
        assertThat(result.maxCapacity()).isEqualTo(10);
        assertThat(result.currentAttendanceCount()).isEqualTo(2);
        assertThat(result.remainingCapacity()).isEqualTo(8);
        assertThat(result.attendees()).hasSize(1);
    }

    @Test
    void getEventAttendeesThrows404WhenTheEventIsUnknown() {
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.empty());

        assertStatus(catchThrowable(() -> service.getEventAttendees(EVENT_ID)), HttpStatus.NOT_FOUND);
    }

    @Test
    void getAttendeeWithEventsListsTheEventsTheUserJoined() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(TestData.user(KARI_ID, "Kari"));
        when(eventAttendanceRepository.findByExternalUserId(KARI_ID))
                .thenReturn(List.of(new EventAttendance(EVENT_ID, KARI_ID)));
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.of(event(10)));
        when(eventAttendanceRepository.findByExternalEventId(EVENT_ID))
                .thenReturn(List.of(new EventAttendance(EVENT_ID, KARI_ID)));

        EventAttendeeOverviewDto result = service.getAttendeeWithEvents(KARI_ID);

        assertThat(result.attendee().externalUserId()).isEqualTo(KARI_ID);
        assertThat(result.joinedEvents()).extracting(EventAttendanceResponseDto::eventName)
                .containsExactly("Summer Picnic");
    }

    @Test
    void getAttendeeWithEventsThrows404WhenTheUserIsUnknown() {
        when(eventClient.getUserByExternalId(KARI_ID)).thenReturn(null);

        assertStatus(catchThrowable(() -> service.getAttendeeWithEvents(KARI_ID)), HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrganizerWithCreatedEventsListsTheOrganizersEvents() {
        when(eventClient.getUserByExternalId(ORGANIZER_ID)).thenReturn(TestData.user(ORGANIZER_ID, "Ola"));
        when(eventRepository.findByEventOrganizer(ORGANIZER_ID)).thenReturn(List.of(event(10)));
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.of(event(10)));
        when(eventAttendanceRepository.findByExternalEventId(EVENT_ID)).thenReturn(List.of());

        EventOrganizerOverviewDto result = service.getOrganizerWithCreatedEvents(ORGANIZER_ID);

        assertThat(result.organizer().username()).isEqualTo("ola");
        assertThat(result.hostedEvents()).hasSize(1);
        assertThat(result.hostedEvents().get(0).remainingCapacity()).isEqualTo(10);
    }

    @Test
    void getOrganizerWithCreatedEventsThrows404WhenTheUserIsUnknown() {
        when(eventClient.getUserByExternalId(ORGANIZER_ID)).thenReturn(null);

        assertStatus(catchThrowable(() -> service.getOrganizerWithCreatedEvents(ORGANIZER_ID)), HttpStatus.NOT_FOUND);
    }

    @Test
    void removeAttendanceDeletesTheRegistration() {
        EventAttendance registration = new EventAttendance(EVENT_ID, KARI_ID);
        when(eventAttendanceRepository.findByExternalEventIdAndExternalUserId(EVENT_ID, KARI_ID))
                .thenReturn(Optional.of(registration));

        service.removeAttendance(EVENT_ID, KARI_ID);

        verify(eventAttendanceRepository).delete(registration);
    }

    @Test
    void removeAttendanceThrows404WhenNotRegistered() {
        when(eventAttendanceRepository.findByExternalEventIdAndExternalUserId(EVENT_ID, KARI_ID))
                .thenReturn(Optional.empty());

        assertStatus(catchThrowable(() -> service.removeAttendance(EVENT_ID, KARI_ID)), HttpStatus.NOT_FOUND);
    }

    @Test
    void getVendorWithHostedEventsListsTheVendorsEvents() {
        when(eventClient.getVendorByExternalId(VENDOR_ID)).thenReturn(TestData.vendor(VENDOR_ID, "Catering AS"));
        when(eventRepository.findByExternalVendorId(VENDOR_ID)).thenReturn(List.of(event(10)));
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.of(event(10)));
        when(eventAttendanceRepository.findByExternalEventId(EVENT_ID)).thenReturn(List.of());

        EventVendorOverviewDto result = service.getVendorWithHostedEvents(VENDOR_ID);

        assertThat(result.vendor().companyName()).isEqualTo("Catering AS");
        assertThat(result.hostedEvents()).hasSize(1);
    }

    @Test
    void getVendorWithHostedEventsThrows404WhenTheVendorIsUnknown() {
        when(eventClient.getVendorByExternalId(VENDOR_ID)).thenReturn(null);

        assertStatus(catchThrowable(() -> service.getVendorWithHostedEvents(VENDOR_ID)), HttpStatus.NOT_FOUND);
    }
}
