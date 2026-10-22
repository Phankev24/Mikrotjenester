package org.epm.event.service;

import org.epm.event.dto.event.*;
import org.epm.event.event.Event;
import org.epm.event.event.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


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

    public List<EventResponseDto> getEventsByEventUUID(UUID externalEventId){
        return eventRepository.findByExternalEventId(externalEventId)
                .stream()
                .map(eventResponseMapper::toDTO)
                .toList();
    }

    public Event createEvent(EventCreateDto eventCreateDto, UUID externalUserId, UUID externalVendorId){
        Event event = eventCreateMapper.toEntity(eventCreateDto, externalUserId, externalVendorId);
        return eventRepository.save(event);
    }
}