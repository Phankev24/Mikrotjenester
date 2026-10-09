package org.epm.event.dto;

import org.epm.event.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventResponseMapper {
    public EventResponseDto toDTO(Event event){
        if(event == null) return null;

        return new EventResponseDto(
                event.getInternalEventId(),
                event.getExternalEventId(),
                event.getExternalUserId(),
                event.getExternalVendorId(),
                event.getEventName(),
                event.getEventDescription(),
                event.getEventCategory(),
                event.getEventLocation(),
                event.getEventDateTime(),
                event.getEventAttendance()
        );
    }
}
