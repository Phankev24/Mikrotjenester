package org.epm.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;
import org.epm.event.service.EventAttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/event/{externalEventId}/attend")
public class EventAttendanceController {
    private final EventAttendanceService attendanceService;

    public EventAttendanceController(EventAttendanceService eventAttendanceService) {
        this.attendanceService = eventAttendanceService;
    }

    @Operation(summary = "Get list of users that are attending this event")
    @GetMapping
    public ResponseEntity<EventAttendanceResponseDto> getEventAttendees(@PathVariable UUID externalEventId) {
        return ResponseEntity.ok(attendanceService.getEventAttendees(externalEventId));
    }

    @Operation(summary = "attendee (user) registers for an event")
    @PostMapping("/{externalUserId}")
    public ResponseEntity<Void> registerAttendee(@PathVariable UUID externalEventId, @PathVariable UUID externalUserId) {
        attendanceService.registerAttendance(externalEventId, externalUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "remove user attendance", description = "Deletes user from attending an event")
    @DeleteMapping("/{externalUserId}")
    public ResponseEntity<Void> removeAttendee(@PathVariable UUID externalEventId, @PathVariable UUID externalUserId){
        attendanceService.removeAttendance(externalEventId, externalUserId);
        return ResponseEntity.noContent().build();
    }
}
