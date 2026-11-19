package org.epm.event.service;

import org.epm.event.TestData;
import org.epm.event.dto.event.EventCreateDto;
import org.epm.event.dto.event.EventCreateMapper;
import org.epm.event.dto.event.EventResponseDto;
import org.epm.event.dto.event.EventResponseMapper;
import org.epm.event.dto.event.EventUpdateDto;
import org.epm.event.dto.event.EventUpdateMapper;
import org.epm.event.enumeration.EventCategory;
import org.epm.event.event.Event;
import org.epm.event.repository.EventAttendanceRepository;
import org.epm.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private static final UUID USER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID VENDOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EVENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventAttendanceRepository eventAttendanceRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, eventAttendanceRepository, new EventResponseMapper(),
                new EventCreateMapper(), new EventUpdateMapper());
    }

    private static Event userEvent(String name) {
        return TestData.event(EVENT_ID, USER_ID, USER_ID, null, name, 20);
    }

    @Test
    void getAllEventsMapsEveryEvent() {
        when(eventRepository.findAll()).thenReturn(List.of(userEvent("Picnic"), userEvent("Party")));

        assertThat(eventService.getAllEvents()).extracting(EventResponseDto::eventName)
                .containsExactly("Picnic", "Party");
    }

    @Test
    void getEventsByEventUuidReturnsTheMatchingEvent() {
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.of(userEvent("Picnic")));

        assertThat(eventService.getEventsByEventUUID(EVENT_ID)).hasSize(1);
    }

    @Test
    void getEventsByEventUuidReturnsEmptyListWhenUnknown() {
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.empty());

        assertThat(eventService.getEventsByEventUUID(EVENT_ID)).isEmpty();
    }

    @Test
    void getEventByVendorAndByUserUseTheirOwnLookups() {
        when(eventRepository.findByExternalVendorId(VENDOR_ID)).thenReturn(List.of(userEvent("Vendor party")));
        when(eventRepository.findByExternalUserId(USER_ID)).thenReturn(List.of(userEvent("Picnic")));

        assertThat(eventService.getEventByVendorUUID(VENDOR_ID)).extracting(EventResponseDto::eventName)
                .containsExactly("Vendor party");
        assertThat(eventService.getEventByUserUUID(USER_ID)).extracting(EventResponseDto::eventName)
                .containsExactly("Picnic");
    }

    @Test
    void createEventSavesTheMappedEntity() {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event saved = eventService.createEvent(
                new EventCreateDto("Picnic", "Community", EventCategory.SOCIAL, "City Park", 50), USER_ID, null);

        assertThat(saved.getEventName()).isEqualTo("Picnic");
        assertThat(saved.getEventOrganizer()).isEqualTo(USER_ID);
        verify(eventRepository).save(saved);
    }

    @Test
    void updateEventChangesTheEventAndReturnsTheDto() {
        Event existing = userEvent("Old name");
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.of(existing));
        when(eventRepository.save(existing)).thenReturn(existing);

        EventResponseDto result = eventService.updateEvent(EVENT_ID,
                new EventUpdateDto("New name", null, null, null, null, null));

        assertThat(existing.getEventName()).isEqualTo("New name");
        assertThat(result.eventName()).isEqualTo("New name");
    }

    @Test
    void updateEventThrows404WhenMissing() {
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(EVENT_ID,
                new EventUpdateDto(null, null, null, null, null, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteEventRemovesTheAttendancesBeforeTheEvent() {
        Event existing = userEvent("Picnic");
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.of(existing));

        eventService.deleteEvent(EVENT_ID);

        var order = inOrder(eventAttendanceRepository, eventRepository);
        order.verify(eventAttendanceRepository).deleteByExternalEventId(EVENT_ID);
        order.verify(eventRepository).delete(existing);
    }

    @Test
    void deleteEventThrows404WhenMissing() {
        when(eventRepository.findByExternalEventId(EVENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.deleteEvent(EVENT_ID))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(eventRepository, never()).delete(any());
    }
}
