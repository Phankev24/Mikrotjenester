package org.epm.event.mapper;

import org.epm.event.TestData;
import org.epm.event.dto.event.EventCreateDto;
import org.epm.event.dto.event.EventCreateMapper;
import org.epm.event.dto.event.EventResponseDto;
import org.epm.event.dto.event.EventResponseMapper;
import org.epm.event.dto.event.EventUpdateDto;
import org.epm.event.dto.event.EventUpdateMapper;
import org.epm.event.enumeration.EventCategory;
import org.epm.event.event.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private static final UUID USER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID VENDOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EVENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final EventCreateMapper createMapper = new EventCreateMapper();
    private final EventUpdateMapper updateMapper = new EventUpdateMapper();
    private final EventResponseMapper responseMapper = new EventResponseMapper();

    private final EventCreateDto createDto =
            new EventCreateDto("Picnic", "Community picnic", EventCategory.SOCIAL, "City Park", 50);

    @Test
    void createMapperMakesTheUserTheOrganizer() {
        Event event = createMapper.toEntity(createDto, USER_ID, null);

        assertThat(event.getEventName()).isEqualTo("Picnic");
        assertThat(event.getEventAttendance()).isEqualTo(50);
        assertThat(event.getExternalUserId()).isEqualTo(USER_ID);
        assertThat(event.getExternalVendorId()).isNull();
        assertThat(event.getEventOrganizer()).isEqualTo(USER_ID);
        assertThat(event.getExternalEventId()).isNotNull();
        assertThat(event.getEventDateTime()).isNotNull();
    }

    @Test
    void createMapperMakesTheVendorTheOrganizerWhenThereIsNoUser() {
        Event event = createMapper.toEntity(createDto, null, VENDOR_ID);

        assertThat(event.getEventOrganizer()).isEqualTo(VENDOR_ID);
        assertThat(event.getExternalVendorId()).isEqualTo(VENDOR_ID);
        assertThat(event.getExternalUserId()).isNull();
    }

    @Test
    void createMapperReturnsNullForNullInput() {
        assertThat(createMapper.toEntity(null, USER_ID, null)).isNull();
    }

    @Test
    void updateMapperAppliesEveryProvidedField() {
        Event event = TestData.event(EVENT_ID, USER_ID, USER_ID, null, "Old name", 10);
        LocalDateTime newTime = LocalDateTime.of(2028, 5, 5, 12, 0);

        updateMapper.updateEventFromDto(
                new EventUpdateDto("New name", "New description", 99, "Bergen", newTime, EventCategory.MUSIC), event);

        assertThat(event.getEventName()).isEqualTo("New name");
        assertThat(event.getEventDescription()).isEqualTo("New description");
        assertThat(event.getEventAttendance()).isEqualTo(99);
        assertThat(event.getEventLocation()).isEqualTo("Bergen");
        assertThat(event.getEventDateTime()).isEqualTo(newTime);
        assertThat(event.getEventCategory()).isEqualTo(EventCategory.MUSIC);
    }

    @Test
    void updateMapperLeavesFieldsUntouchedWhenTheyAreNull() {
        Event event = TestData.event(EVENT_ID, USER_ID, USER_ID, null, "Old name", 10);

        updateMapper.updateEventFromDto(new EventUpdateDto(null, null, null, null, null, null), event);

        assertThat(event.getEventName()).isEqualTo("Old name");
        assertThat(event.getEventAttendance()).isEqualTo(10);
        assertThat(event.getEventLocation()).isEqualTo("Oslo");
        assertThat(event.getEventCategory()).isEqualTo(EventCategory.SOCIAL);
    }

    @Test
    void responseMapperCopiesEveryField() {
        Event event = TestData.event(EVENT_ID, USER_ID, USER_ID, null, "Picnic", 50);

        EventResponseDto dto = responseMapper.toDTO(event);

        assertThat(dto.externalEventId()).isEqualTo(EVENT_ID);
        assertThat(dto.externalUserId()).isEqualTo(USER_ID);
        assertThat(dto.eventOrganizer()).isEqualTo(USER_ID);
        assertThat(dto.eventName()).isEqualTo("Picnic");
        assertThat(dto.eventAttendance()).isEqualTo(50);
        assertThat(dto.eventCategory()).isEqualTo(EventCategory.SOCIAL);
    }

    @Test
    void responseMapperReturnsNullForNullInput() {
        assertThat(responseMapper.toDTO(null)).isNull();
    }
}
