package org.epm.event.service;

import org.epm.event.dto.EventDto;
import org.epm.event.event.Event;
import org.epm.event.event.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class EventService {
    private final EventRepository eventRepository;
    public EventService(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    public List<Event> findAll(){
        return eventRepository.findAll();
    }

    public Event createEvent(EventDto eventDto){
        Event event = new Event();
        event.setEventName(eventDto.getEventName());
        event.setEventDescription(eventDto.getEventDescription());
        event.setEventAttendance(eventDto.getEventAttendance());
        event.setEventDateTime(eventDto.getEventDateTime());
        event.setEventCategory(eventDto.getEventCategory());

        return eventRepository.save(event);
    }
}
