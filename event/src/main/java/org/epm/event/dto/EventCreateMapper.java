package org.epm.event.dto;

import org.epm.event.event.Event;
import org.epm.event.event.EventRepository;
import org.springframework.stereotype.Component;

@Component
public class EventCreateMapper {
    private final EventRepository eventRepository;

    public EventCreateMapper(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event toEntity(EventCreateDto eventCreateDto){
        if(eventCreateDto == null) return null;

        Event event = new Event();
        event.setExternalVendorId(eventCreateDto.externalVendorId());
        event.setEventName(eventCreateDto.eventName());
        event.setEventDescription(eventCreateDto.eventDescription());
        event.setEventCategory(eventCreateDto.eventCategory());
        event.setEventLocation(eventCreateDto.eventLocation());
        event.setEventAttendance(eventCreateDto.eventAttendance());

        return event;
    }
}
