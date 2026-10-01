package org.epm.event.controller;

import org.epm.event.dto.EventCreateDto;
import org.epm.event.dto.EventResponseDto;
import org.epm.event.dto.EventUpdateDto;
import org.epm.event.service.EventService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<EventResponseDto>> getAllEvents(){
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id){
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody EventCreateDto eventCreateDto){
        EventResponseDto createdEvent = eventService.createEvent(eventCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable Long id, @RequestBody EventUpdateDto eventUpdateDto){
        EventResponseDto updatedEvent = eventService.updateEvent(id, eventUpdateDto);

        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id){
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
