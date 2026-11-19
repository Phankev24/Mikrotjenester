package org.epm.event.service;

import lombok.extern.slf4j.Slf4j;
import org.epm.event.dto.event.*;
import org.epm.event.event.Event;
import org.epm.event.repository.EventAttendanceRepository;
import org.epm.event.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventResponseMapper eventResponseMapper;
    private final EventCreateMapper eventCreateMapper;
    private final EventUpdateMapper eventUpdateMapper;

    public EventService(EventRepository eventRepository, EventAttendanceRepository eventAttendanceRepository, EventResponseMapper eventResponseMapper, EventCreateMapper eventCreateMapper, EventUpdateMapper eventUpdateMapper) {
        this.eventRepository = eventRepository;
        this.eventAttendanceRepository = eventAttendanceRepository;
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
        Event saved = eventRepository.save(event);
        log.info("Event created: '{}' ({}) by {} {}", saved.getEventName(), saved.getExternalEventId(),
                externalUserId != null ? "user" : "vendor",
                externalUserId != null ? externalUserId : externalVendorId);
        return saved;
    }

    public EventResponseDto updateEvent(UUID externalEventId, EventUpdateDto eventUpdateDto){
        Event event = eventRepository.findByExternalEventId(externalEventId)
                .orElseThrow(() -> eventNotFound(externalEventId));
        eventUpdateMapper.updateEventFromDto(eventUpdateDto, event);
        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated: {}", externalEventId);
        return eventResponseMapper.toDTO(updatedEvent);
    }

    @Transactional
    public void deleteEvent(UUID externalEventId) {
        Event event = eventRepository.findByExternalEventId(externalEventId)
                .orElseThrow(() -> eventNotFound(externalEventId));
        eventAttendanceRepository.deleteByExternalEventId(externalEventId);
        eventRepository.delete(event);
        log.info("Event deleted together with its registrations: {}", externalEventId);
    }

    private ResponseStatusException eventNotFound(UUID externalEventId) {
        log.warn("Event not found: {}", externalEventId);
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found with this id: " + externalEventId);
    }
}
