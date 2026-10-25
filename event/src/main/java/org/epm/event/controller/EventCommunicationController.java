package org.epm.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.epm.event.client.EventClient;
import org.epm.event.dto.event.EventResponseDto;
import org.epm.event.dto.user.EventUserResponseDto;
import org.epm.event.dto.vendor.EventVendorResponseDto;
import org.epm.event.dto.user.UserResponseDto;
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

    @Operation(description = "Gets all vendors and their events")
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

    @Operation(description = "Gets ONE vendor and their events")
    @GetMapping("/{externalId}/vendorEvent")
    public ResponseEntity<EventVendorResponseDto> getVendorWithEvents(@PathVariable UUID externalId){
        VendorResponseDto vendorDto = eventClient.getVendorByExternalId(externalId);
        List<EventResponseDto> events = eventService.getEventByVendorId(externalId);

        return ResponseEntity.ok(new EventVendorResponseDto(vendorDto, events));
    }

    @Operation(description = "ONLY GETS USERS")
    @GetMapping("/usersOnly")
    public ResponseEntity<List<UserResponseDto>> getUsers(){
        List<UserResponseDto> users = eventClient.getUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(description = "Gets all users and their events")
    @GetMapping("/usersWithEvents")
    public ResponseEntity<List<EventUserResponseDto>> getAllUsersWithEvents(){
        List<UserResponseDto> allUsers = eventClient.getUsers();
        List<EventResponseDto> allEvents = eventService.getAllEvents();

        Map<UUID, List<EventResponseDto>> eventsByUserId = allEvents
                .stream()
                .filter(event -> event.externalUserId() != null)
                .collect(Collectors.groupingBy(EventResponseDto::externalUserId));

        List<EventUserResponseDto> result = allUsers
                .stream()
                .map(user -> new EventUserResponseDto(
                        user,
                        eventsByUserId.getOrDefault(user.externalUserId(), Collections.emptyList())
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}
