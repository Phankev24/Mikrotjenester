package org.epm.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.epm.event.dto.event.EventCreateDto;
import org.epm.event.dto.event.EventResponseDto;
import org.epm.event.dto.event.EventUpdateDto;
import org.epm.event.dto.event.EventUpdateMapper;
import org.epm.event.event.Event;
import org.epm.event.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;
    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents(){
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{externalEventId}")
    public ResponseEntity<List<EventResponseDto>> getEventByEventUUID(@PathVariable UUID externalEventId){
        return ResponseEntity.ok(eventService.getEventsByEventUUID(externalEventId));
    }

    @PostMapping("/user/{externalUserId}")
    public ResponseEntity<EventResponseDto> createUserEvent(@PathVariable UUID externalUserId, @RequestBody EventCreateDto eventCreateDto){
        Event event = eventService.createEvent(eventCreateDto, externalUserId, null);
        EventResponseDto eventResponseDto = new EventResponseDto(
                event.getInternalEventId(),
                event.getExternalEventId(),
                event.getExternalUserId(),
                event.getExternalVendorId(),
                event.getEventOrganizer(),
                event.getEventName(),
                event.getEventDescription(),
                event.getEventCategory(),
                event.getEventLocation(),
                event.getEventDateTime(),
                event.getEventAttendance()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponseDto);
    }

    @PostMapping("/vendor/{externalVendorId}")
    public ResponseEntity<EventResponseDto> createVendorEvent(@PathVariable UUID externalVendorId ,@RequestBody EventCreateDto eventCreateDto) {
        Event event = eventService.createEvent(eventCreateDto, null, externalVendorId);
        EventResponseDto eventResponseDto = new EventResponseDto(
                event.getInternalEventId(),
                event.getExternalEventId(),
                event.getExternalUserId(),
                event.getExternalVendorId(),
                event.getEventOrganizer(),
                event.getEventName(),
                event.getEventDescription(),
                event.getEventCategory(),
                event.getEventLocation(),
                event.getEventDateTime(),
                event.getEventAttendance()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponseDto);
    }

    @Operation(description = "Updates Event Details, only updates event details. own user details update and vendor coming soon")
    @PatchMapping("/{externalEventId}")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable UUID externalEventId, @RequestBody EventUpdateDto eventUpdateDto){
        EventResponseDto updatedEvent = eventService.updateEvent(externalEventId, eventUpdateDto);
        return ResponseEntity.ok(updatedEvent);
    }
}
