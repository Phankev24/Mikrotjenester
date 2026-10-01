package org.epm.event.dto;

import org.epm.event.event.Event;
import org.epm.event.event.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class EventCreateMapper {
    private final EventRepository eventRepository;

    public EventCreateMapper(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventCreateDto toDTO(Event event){
        if(event == null) return null;

        return new EventCreateDto(
                event.getEventName(),
                event.getEventDescription(),
                event.getEventAttendance(),
                event.getEventCategory()
        );
    }

    public Event toEntity(EventCreateDto eventCreateDto){
        if(eventCreateDto == null) return null;

        Event event = new Event();
        event.setEventName(eventCreateDto.eventName());
        event.setEventDescription(eventCreateDto.eventDescription());
        event.setEventAttendance(eventCreateDto.eventAttendance());
        event.setEventCategory(eventCreateDto.eventCategory());

        return event;
    }
}
