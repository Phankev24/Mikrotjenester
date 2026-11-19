package org.epm.event.service;

import lombok.extern.slf4j.Slf4j;
import org.epm.event.client.EventClient;
import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;
import org.epm.event.dto.user.EventAttendeeOverviewDto;
import org.epm.event.dto.user.EventOrganizerOverviewDto;
import org.epm.event.dto.user.UserResponseDto;
import org.epm.event.dto.vendor.EventVendorOverviewDto;
import org.epm.event.dto.vendor.VendorResponseDto;
import org.epm.event.event.Event;
import org.epm.event.event.EventAttendance;
import org.epm.event.kafka.EventJoinedEvent;
import org.epm.event.kafka.EventKafkaProducer;
import org.epm.event.repository.EventAttendanceRepository;
import org.epm.event.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class EventAttendanceService {
    private final EventRepository eventRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final EventClient eventClient;
    private final EventKafkaProducer eventKafkaProducer;

    public EventAttendanceService(EventRepository eventRepository, EventAttendanceRepository eventAttendanceRepository, EventClient eventClient, EventKafkaProducer eventKafkaProducer) {
        this.eventRepository = eventRepository;
        this.eventAttendanceRepository = eventAttendanceRepository;
        this.eventClient = eventClient;
        this.eventKafkaProducer = eventKafkaProducer;
    }

    @Transactional
    public void registerAttendance(UUID externalEventId, UUID externalUserId) {
        UserResponseDto user = eventClient.getUserByExternalId(externalUserId);
        if (user == null) {
            throw reject(HttpStatus.NOT_FOUND, "User not found: " + externalUserId);
        }

        Event event = eventRepository.findByExternalEventIdForUpdate(externalEventId)
                .orElseThrow(() -> reject(HttpStatus.NOT_FOUND, "Event not found: " + externalEventId));

        if (eventAttendanceRepository.existsByExternalEventIdAndExternalUserId(externalEventId, externalUserId)) {
            throw reject(HttpStatus.CONFLICT, "User is already registered for this event.");
        }

        int maxCapacity = event.getEventAttendance() != null ? event.getEventAttendance() : 0;
        int currentCount = eventAttendanceRepository.countByExternalEventId(externalEventId);

        if (currentCount >= maxCapacity) {
            throw reject(HttpStatus.BAD_REQUEST, "Event has reached maximum capacity (" + maxCapacity + ").");
        }

        eventAttendanceRepository.save(new EventAttendance(externalEventId, externalUserId));
        log.info("User {} registered for event {} ({}/{})", externalUserId, externalEventId, currentCount + 1, maxCapacity);

        String attendeeName = user.firstName() + " " + user.lastName();

        publishAfterCommit(new EventJoinedEvent(
                event.getExternalEventId(),
                event.getEventName(),
                event.getEventOrganizer(),
                externalUserId,
                attendeeName,
                user.email(),
                Instant.now()
        ));
    }

    @Transactional(readOnly = true)
    public EventAttendanceResponseDto getEventAttendees(UUID externalEventId) {
        Event event = eventRepository.findByExternalEventId(externalEventId)
                .orElseThrow(() -> reject(HttpStatus.NOT_FOUND, "Event not found: " + externalEventId));

        List<EventAttendance> registrations = eventAttendanceRepository.findByExternalEventId(externalEventId);

        List<UserResponseDto> attendees = registrations.stream()
                .map(r -> {
                    UserResponseDto attendee = eventClient.getUserByExternalId(r.getExternalUserId());
                    if (attendee == null) {
                        log.warn("User {} is registered for event {} but is unknown to the user service",
                                r.getExternalUserId(), externalEventId);
                    }
                    return attendee;
                })
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
            throw reject(HttpStatus.NOT_FOUND, "user not found" + externalUserId);
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
            throw reject(HttpStatus.NOT_FOUND, "User not found: " + externalUserId);
        }

        List<EventAttendanceResponseDto> userEvents = eventRepository.findByEventOrganizer(externalUserId)
                .stream()
                .map(event -> getEventAttendees(event.getExternalEventId()))
                .toList();
        return new EventOrganizerOverviewDto(user, userEvents);
    }

    @Transactional
    public void removeAttendance(UUID externalEventId, UUID externalUserId){
        EventAttendance registration = eventAttendanceRepository.findByExternalEventIdAndExternalUserId(externalEventId, externalUserId)
                .orElseThrow(() -> reject(HttpStatus.NOT_FOUND, "This user: " + externalUserId +" was not registered to this event: " + externalEventId));
        eventAttendanceRepository.delete(registration);
        log.info("User {} removed from event {}", externalUserId, externalEventId);
    }


    @Transactional
    public EventVendorOverviewDto getVendorWithHostedEvents(UUID externalVendorId){
        VendorResponseDto vendor = eventClient.getVendorByExternalId(externalVendorId);
        if(vendor == null){
            throw reject(HttpStatus.NOT_FOUND, "Vendor not found: " + externalVendorId);
        }

        List<EventAttendanceResponseDto> hostedEvents = eventRepository.findByExternalVendorId(externalVendorId)
                .stream()
                .map(event -> getEventAttendees(event.getExternalEventId()))
                .toList();
        return new EventVendorOverviewDto(vendor, hostedEvents);
    }

    private void publishAfterCommit(EventJoinedEvent message) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(message);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(message);
            }
        });
    }

    private void publish(EventJoinedEvent message) {
        try {
            eventKafkaProducer.publishEventJoined(message);
        } catch (RuntimeException e) {
            log.error("Could not publish the join of user {} to event {}", message.attendeeUserId(),
                    message.externalEventId(), e);
        }
    }

    private ResponseStatusException reject(HttpStatus status, String message) {
        log.warn("{}", message);
        return new ResponseStatusException(status, message);
    }
}
