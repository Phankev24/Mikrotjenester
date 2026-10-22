package org.epm.event.controller;

import org.epm.event.dto.event.EventResponseDto;
import org.epm.event.service.EventService;
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

}
