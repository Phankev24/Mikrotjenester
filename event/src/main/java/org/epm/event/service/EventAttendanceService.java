package org.epm.event.service;

import org.epm.event.client.EventClient;
import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;
import org.epm.event.dto.user.EventAttendeeOverviewDto;
import org.epm.event.dto.user.EventOrganizerOverviewDto;
import org.epm.event.dto.user.UserResponseDto;
import org.epm.event.event.Event;
import org.epm.event.event.EventAttendance;
import org.epm.event.repository.EventAttendanceRepository;
import org.epm.event.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventAttendanceService {
    private final EventRepository eventRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventClient eventClient;

    public EventAttendanceService(EventRepository eventRepository, EventAttendanceRepository eventAttendanceRepository, EventClient eventClient) {
        this.eventRepository = eventRepository;
        this.eventAttendanceRepository = eventAttendanceRepository;
        this.eventClient = eventClient;
    }

    @Transactional
    public void registerAttendance(UUID externalEventId, UUID externalUserId) {
        UserResponseDto user = eventClient.getUserByExternalId(externalUserId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + externalUserId);
        }

        Event event = eventRepository.findByExternalEventIdForUpdate(externalEventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + externalEventId));

        if (eventAttendanceRepository.existsByExternalEventIdAndExternalUserId(externalEventId, externalUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already registered for this event.");
        }

        int maxCapacity = event.getEventAttendance() != null ? event.getEventAttendance() : 0;
        int currentCount = eventAttendanceRepository.countByExternalEventId(externalEventId);

        if (currentCount >= maxCapacity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event has reached maximum capacity (" + maxCapacity + ").");
        }

        eventAttendanceRepository.save(new EventAttendance(externalEventId, externalUserId));
    }

    @Transactional(readOnly = true)
    public EventAttendanceResponseDto getEventAttendees(UUID externalEventId) {
        Event event = eventRepository.findByExternalEventId(externalEventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + externalEventId));

        List<EventAttendance> registrations = eventAttendanceRepository.findByExternalEventId(externalEventId);

        List<UserResponseDto> attendees = registrations.stream()
                .map(r -> eventClient.getUserByExternalId(r.getExternalUserId()))
                .filter(Objects::nonNull)
                .toList();

        int currentCount = registrations.size();
        int maxCapacity = event.getEventAttendance() != null ? event.getEventAttendance() : 0;
        int remainingCapacity = Math.max(0, maxCapacity - currentCount);

        return new EventAttendanceResponseDto(
                externalEventId,
                event.getEventName(),
                maxCapacity,
                currentCount,
                remainingCapacity,
                attendees
        );
    }

    @Transactional(readOnly = true)
    public EventAttendeeOverviewDto getAttendeeWithEvents(UUID externalUserId){
        UserResponseDto user = eventClient.getUserByExternalId(externalUserId);
        if(user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found" + externalUserId);
        }

        List<EventAttendanceResponseDto> joinedEvents = eventAttendanceRepository.findByExternalUserId(externalUserId)
                .stream()
                .map(eventAttendance -> getEventAttendees(eventAttendance.getExternalEventId()))
                .toList();

        return new EventAttendeeOverviewDto(user, joinedEvents);
    }

    @Transactional(readOnly = true)
    public EventOrganizerOverviewDto getOrganizerWithCreatedEvents(UUID externalUserId){
        UserResponseDto user = eventClient.getUserByExternalId(externalUserId);
        if(user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + externalUserId);
        }

        List<EventAttendanceResponseDto> userEvents = eventRepository.findByEventOrganizer(externalUserId)
                .stream()
                .map(event -> getEventAttendees(event.getExternalEventId()))
                .toList();
        return new EventOrganizerOverviewDto(user, userEvents);
    }
}
