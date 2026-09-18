package org.epm.event.controller;

import org.epm.event.client.EventClient;
import org.epm.event.dto.EventDto;
import org.epm.event.dto.EventWithVendorsDto;
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

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id){
        return ResponseEntity.ok(eventService.findEventById(id));
    }

    @GetMapping("/vendor")
    public ResponseEntity<List<VendorDto>> fetchVendorViaRestTemplate(){
        return ResponseEntity.ok(eventClient.getVendors());
    }

    @GetMapping("/{id}/with-vendors")
    public ResponseEntity<EventWithVendorsDto> getEventWithVendors(@PathVariable Long id){
        return ResponseEntity.ok(eventService.getEventWithVendors(id));
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody EventDto eventDto){
        Event event = eventService.createEvent(eventDto);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Event> patchEvent(@PathVariable Long id, @RequestBody EventDto eventDto){
        Event patchEvent = eventService.patchEvent(id, eventDto);
        return ResponseEntity.ok(patchEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id){
        eventService.deleteById(id);
        return ResponseEntity.ok("Event {id} deleted");
    }
}
