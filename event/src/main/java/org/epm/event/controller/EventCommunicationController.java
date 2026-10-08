package org.epm.event.controller;

import org.epm.event.client.EventClient;
import org.epm.event.dto.VendorResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/client/event")
public class EventCommunicationController {
    private final EventClient eventClient;

    public EventCommunicationController(EventClient eventClient) {
        this.eventClient = eventClient;
    }

    @GetMapping("/vendor")
    public ResponseEntity<List<VendorResponseDto>> getAllVendors(){
        return ResponseEntity.ok(eventClient.getAllVendors());
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<VendorResponseDto>> getVendorsByVendorId(@PathVariable UUID vendorId){
        return ResponseEntity.ok(eventClient.getVendorsByVendorId(vendorId));
    }
}
