package org.epm.event.service;

import org.epm.event.dto.*;
import org.epm.event.event.Event;
import org.epm.event.event.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventResponseMapper eventResponseMapper;
    private final EventCreateMapper eventCreateMapper;

    public EventService(EventRepository eventRepository, EventResponseMapper eventResponseMapper, EventCreateMapper eventCreateMapper) {
        this.eventRepository = eventRepository;
        this.eventResponseMapper = eventResponseMapper;
        this.eventCreateMapper = eventCreateMapper;
    }

    public List<EventResponseDto> getAllEvents(){
        return eventRepository.findAll()
                .stream()
                .map(eventResponseMapper::toDTO)
                .toList();
    }

    public EventResponseDto getEventById(Long id){
        return eventRepository.findById(id)
                .map(eventResponseMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Event not found with this id" + id)) ;
    }

    public EventResponseDto createEvent(EventCreateDto eventCreateDto){
        Event eventEntity = eventCreateMapper.toEntity(eventCreateDto);
        Event savedEvent = eventRepository.save(eventEntity);
        return eventResponseMapper.toDTO(savedEvent);
    }

    public EventResponseDto updateEvent(Long id, EventUpdateDto eventUpdateDto){
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with this id: " + id));

        if(eventUpdateDto.eventName() != null){
            event.setEventName(eventUpdateDto.eventName());
        }

        if(eventUpdateDto.eventDescription() != null){
            event.setEventDescription(eventUpdateDto.eventDescription());
        }

        if(eventUpdateDto.eventAttendance() > 0){
            event.setEventAttendance(eventUpdateDto.eventAttendance());
        }

        if(eventUpdateDto.eventCategory()!= null){
            event.setEventCategory(eventUpdateDto.eventCategory());
        }

        if(eventUpdateDto.eventDateTime() != null){
            event.setEventDateTime(eventUpdateDto.eventDateTime());
        }

        Event updatedEvent = eventRepository.save(event);

        return eventResponseMapper.toDTO(updatedEvent);
    }

    public void deleteEvent(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
        eventRepository.delete(event);
    }
}