package org.epm.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.annotations.OpenAPI30;
import org.epm.event.client.EventClient;
import org.epm.event.dto.event.EventResponseDto;
import org.epm.event.dto.event.EventVendorResponseDto;
import org.epm.event.dto.vendor.VendorResponseDto;
import org.epm.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/client/event")
public class EventCommunicationController {
    private final EventClient eventClient;
    private final EventService eventService;

    public EventCommunicationController(EventClient eventClient, EventService eventService) {
        this.eventClient = eventClient;
        this.eventService = eventService;
    }

    @Operation(description = "ONLY GETS VENDORS")
    @GetMapping("/vendorsOnly")
    public ResponseEntity<List<VendorResponseDto>> getVendors() {
        List<VendorResponseDto> vendors = eventClient.getVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/vendorsWithEvents")
    public ResponseEntity<List<EventVendorResponseDto>> getAllVendorsWithEvents() {
        List<VendorResponseDto> allVendors = eventClient.getVendors();
        List<EventResponseDto> allEvents = eventService.getAllEvents();

        Map<UUID, List<EventResponseDto>> eventsByVendorId = allEvents
                .stream()
                .filter(event -> event.externalVendorId() != null)
                .collect(Collectors.groupingBy(EventResponseDto::externalVendorId));

        List<EventVendorResponseDto> result = allVendors
                .stream()
                .map(vendor -> new EventVendorResponseDto(
                        vendor,
                        eventsByVendorId.getOrDefault(vendor.externalId(), Collections.emptyList())
                ))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{externalId}/vendorEvent")
    public ResponseEntity<EventVendorResponseDto> getVendorWithEvents(@PathVariable UUID externalId){
        VendorResponseDto vendorDto = eventClient.getVendorByExternalId(externalId);
        List<EventResponseDto> events = eventService.getEventByVendorId(externalId);

        return ResponseEntity.ok(new EventVendorResponseDto(vendorDto, events));
    }
}
