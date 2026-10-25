package org.epm.event.dto.event;

import org.epm.event.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventUpdateMapper {
    public void updateEventFromDto(EventUpdateDto eventUpdateDto, Event event){
        if(eventUpdateDto.eventName() != null ){
            event.setEventName(eventUpdateDto.eventName());
        }

        if(eventUpdateDto.eventDescription() != null){
            event.setEventDescription(eventUpdateDto.eventDescription());
        }

        if(eventUpdateDto.eventAttendance() != null){
            event.setEventAttendance(eventUpdateDto.eventAttendance());
        }

        if(eventUpdateDto.eventLocation() != null){
            event.setEventLocation(eventUpdateDto.eventLocation());
        }

        if(eventUpdateDto.eventDateTime() != null){
            event.setEventDateTime(eventUpdateDto.eventDateTime());
        }

        if(eventUpdateDto.eventCategory() != null){
            event.setEventCategory(eventUpdateDto.eventCategory());
        }
    }
}
