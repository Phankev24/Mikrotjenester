package org.epm.event.service;

import org.epm.event.dto.event.*;
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
        Event updatedEvent = eventRepository.save(event);
        return eventResponseMapper.toDTO(updatedEvent);
    }

    public void deleteEvent(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
        eventRepository.delete(event);
    }
}