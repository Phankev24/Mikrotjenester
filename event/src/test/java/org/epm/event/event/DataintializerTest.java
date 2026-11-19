package org.epm.event.event;

import org.epm.event.enumeration.EventCategory;
import org.epm.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataintializerTest {

    @Mock
    private EventRepository eventRepository;

    @Test
    void seedsTheTwoDemoEventsWithFixedIds() throws Exception {
        new Dataintializer().initDatabase(eventRepository).run();

        ArgumentCaptor<Event> saved = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(Event::getEventName)
                .containsExactly("Tech Conference 2023", "Summer Picnic");
        assertThat(saved.getAllValues()).extracting(Event::getEventCategory)
                .containsExactly(EventCategory.CONFERENCE, EventCategory.SOCIAL);
        assertThat(saved.getAllValues().get(1).getExternalEventId())
                .isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(saved.getAllValues().get(1).getExternalUserId())
                .isEqualTo(UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe"));
    }
}
