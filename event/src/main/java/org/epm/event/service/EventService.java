package org.epm.event.service;

import org.epm.event.dto.event.*;
import org.epm.event.event.Event;
import org.epm.event.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;


@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventResponseMapper eventResponseMapper;
    private final EventCreateMapper eventCreateMapper;
    private final EventUpdateMapper eventUpdateMapper;

    public EventService(EventRepository eventRepository, EventResponseMapper eventResponseMapper, EventCreateMapper eventCreateMapper, EventUpdateMapper eventUpdateMapper) {
        this.eventRepository = eventRepository;
        this.eventResponseMapper = eventResponseMapper;
        this.eventCreateMapper = eventCreateMapper;
        this.eventUpdateMapper = eventUpdateMapper;
    }

    public List<EventResponseDto> getAllEvents(){
        return eventRepository.findAll()
                .stream()
                .map(eventResponseMapper::toDTO)
                .toList();
    }

    public List<EventResponseDto> getEventsByEventUUID(UUID externalEventId){
        return eventRepository.findByExternalEventId(externalEventId)
                .stream()
                .map(eventResponseMapper::toDTO)
                .toList();
    }

    public List<EventResponseDto> getEventByVendorUUID(UUID externalId){
        return eventRepository.findByExternalVendorId(externalId)
                .stream()
                .map(eventResponseMapper::toDTO)
                .toList();
    }

    public List<EventResponseDto> getEventByUserUUID(UUID externalUserId){
        return eventRepository.findByExternalUserId(externalUserId)
                .stream()
                .map(eventResponseMapper::toDTO)
                .toList();
    }

    public Event createEvent(EventCreateDto eventCreateDto, UUID externalUserId, UUID externalVendorId){
        Event event = eventCreateMapper.toEntity(eventCreateDto, externalUserId, externalVendorId);
        return eventRepository.save(event);
    }

    public EventResponseDto updateEvent(UUID externalEventId, EventUpdateDto eventUpdateDto){
        Event event = eventRepository.findByExternalEventId(externalEventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with this id: " + externalEventId));
        eventUpdateMapper.updateEventFromDto(eventUpdateDto, event);
        Event updatedEvent = eventRepository.save(event);
        return eventResponseMapper.toDTO(updatedEvent);
    }
}