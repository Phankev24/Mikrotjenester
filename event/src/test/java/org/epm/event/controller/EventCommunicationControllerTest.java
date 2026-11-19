package org.epm.event.controller;

import org.epm.event.client.EventClient;
import org.epm.event.service.EventAttendanceService;
import org.epm.event.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventCommunicationControllerTest {

    @Mock
    private EventClient eventClient;
    @Mock
    private EventService eventService;
    @Mock
    private EventAttendanceService eventAttendanceService;

    @Test
    void vendorWithEventsGives404WhenTheVendorServiceDoesNotKnowTheVendor() {
        UUID vendorId = UUID.randomUUID();
        when(eventClient.getVendorByExternalId(vendorId)).thenReturn(null);
        EventCommunicationController controller = new EventCommunicationController(eventClient, eventService, eventAttendanceService);

        assertThatThrownBy(() -> controller.getVendorWithEvents(vendorId))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(eventService, never()).getEventByVendorUUID(any());
    }
}
