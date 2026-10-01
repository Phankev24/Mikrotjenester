package org.epm.event.controller;

import org.epm.event.client.EventClient;
import org.epm.event.dto.VendorResponseDto;
import org.epm.event.event.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/client/event")
public class EventCommunicationController {
    private final EventClient eventClient;
    private final EventRepository eventRepository;

    public EventCommunicationController(EventClient eventClient, EventRepository eventRepository) {
        this.eventClient = eventClient;
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public ResponseEntity<List<VendorResponseDto>> getAllVendors(){
        return ResponseEntity.ok(eventClient.getAllVendors());
    }
}
