package org.epm.event.dto.event;

import org.epm.event.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventUpdateMapper {
    public void updateEntityFromDto(EventUpdateDto eventUpdateDto, Event event){
        if(eventUpdateDto.eventOrganizer() != null){
            event.setEventOrganizer(event.getEventOrganizer());
        }

        if(eventUpdateDto.eventName() != null){
            event.setEventName(event.getEventName());
        }

        if(eventUpdateDto.eventDescription() != null){
            event.setEventDescription(event.getEventDescription());
        }

        if(eventUpdateDto.eventCategory() != null){
            event.setEventCategory(event.getEventCategory());
        }

        if(eventUpdateDto.eventLocation() != null){
            event.setEventLocation(event.getEventLocation());
        }

        if (eventUpdateDto.eventLocation() != null) {
            event.setEventLocation(event.getEventLocation());
        }

        if(eventUpdateDto.eventAttendance() != null){
            event.setEventAttendance(event.getEventAttendance());
        }
    }
}
