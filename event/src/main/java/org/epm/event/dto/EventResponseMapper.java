package org.epm.event.dto;

import org.epm.event.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventResponseMapper {
    public EventResponseDto toDTO(Event event){
        if(event == null) return null;

        return new EventResponseDto(
                event.getEventId(),
                event.getVendorId(),
                event.getEventName(),
                event.getEventDescription(),
                event.getEventAttendance(),
                event.getEventCategory()
        );
    }
}
