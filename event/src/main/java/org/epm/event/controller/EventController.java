package org.epm.event.controller;

import org.epm.event.client.EventClient;
import org.epm.event.dto.EventDto;
import org.epm.event.dto.VendorDto;
import org.epm.event.event.Event;
import org.epm.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;
    private final EventClient eventClient;

    public EventController(EventService eventService, EventClient eventClient){
        this.eventService = eventService;
        this.eventClient = eventClient;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(){
        return ResponseEntity.ok(eventService.findAll());
    }

    @GetMapping("/client/event")
    public ResponseEntity<List<EventDto>> fetchViaRestTemplate(){
        return ResponseEntity.ok(eventClient.getEvents());
    }

    @GetMapping("/vendor")
    public ResponseEntity<List<VendorDto>> fetchVendorViaRestTemplate(){
        return ResponseEntity.ok(eventClient.getVendors());
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventDto eventDto){
        Event event = eventService.createEvent(eventDto);
        System.out.println("Fetched Vendor from different service!!");
        return ResponseEntity.ok(event);
    }
}
