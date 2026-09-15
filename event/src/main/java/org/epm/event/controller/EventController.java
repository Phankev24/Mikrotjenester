package org.epm.event.controller;

import org.epm.event.dto.EventDto;
import org.epm.event.event.Event;
import org.epm.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(){
        return ResponseEntity.ok(eventService.findAll());
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventDto eventDto){
        Event event = eventService.createEvent(eventDto);
        return ResponseEntity.ok(event);
    }
}
