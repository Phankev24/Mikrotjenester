package org.epm.event.service;

import org.epm.event.client.EventClient;
import org.epm.event.dto.EventDto;
import org.epm.event.dto.EventWithVendorsDto;
import org.epm.event.event.Event;
import org.epm.event.event.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventClient eventClient;

    public EventService(EventRepository eventRepository, EventClient eventClient) {
        this.eventRepository = eventRepository;
        this.eventClient = eventClient;
    }


    public List<Event> findAll() {
        return eventRepository.findAll();
    }


    public Event findEventById(Long id){
        return eventRepository.findById(id).orElse(null);
    }


    public Event createEvent(EventDto eventDto) {
        Event event = new Event();
        event.setEventName(eventDto.getEventName());
        event.setEventDescription(eventDto.getEventDescription());
        event.setEventAttendance(eventDto.getEventAttendance());
        event.setEventDateTime(eventDto.getEventDateTime());
        event.setEventCategory(eventDto.getEventCategory());
        event.setVendorIds(eventDto.getVendorIds());
        return eventRepository.save(event);
    }
    


    public EventWithVendorsDto getEventWithVendors(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + eventId));

        return new EventWithVendorsDto(
                event.getEventId(),
                event.getEventName(),
                event.getEventDescription(),
                event.getEventAttendance(),
                event.getEventDateTime(),
                event.getEventCategory(),
                eventClient.getVendorsByIds(event.getVendorIds())
        );
    }


    public void deleteById(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
        eventRepository.delete(event);
    }


    public Event patchEvent(Long id, EventDto eventDto){
        Event existingEvent = findEventById(id);

        if(eventDto.getEventName() != null){
            existingEvent.setEventName(eventDto.getEventName());
        }

        if(eventDto.getEventDescription() != null){
            existingEvent.setEventDescription(eventDto.getEventDescription());
        }

        if(eventDto.getEventAttendance() != 0){
            existingEvent.setEventAttendance(eventDto.getEventAttendance());
        }

        if(eventDto.getEventDateTime() != null){
            existingEvent.setEventDateTime(eventDto.getEventDateTime());
        }

        if(eventDto.getEventCategory() != null){
            existingEvent.setEventCategory(eventDto.getEventCategory());
        }

        if(eventDto.getVendorIds() != null && !eventDto.getVendorIds().isEmpty()){
            existingEvent.setVendorIds(eventDto.getVendorIds());
        }

        return eventRepository.save(existingEvent);
    }
}