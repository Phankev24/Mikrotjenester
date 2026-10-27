package org.epm.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;
import org.epm.event.service.EventAttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/event/{externalEventId}/attendances")
public class EventAttendanceController {
    private final EventAttendanceService attendanceService;

    public EventAttendanceController(EventAttendanceService eventAttendanceService) {
        this.attendanceService = eventAttendanceService;
    }

    @Operation(summary = "Register a user for an event")
    @PostMapping("/{externalUserId}")
    public ResponseEntity<Void> registerAttendee(@PathVariable UUID externalEventId,
                                                 @PathVariable UUID externalUserId) {
        attendanceService.registerAttendee(externalEventId, externalUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(summary = "Get full attendee list and capacity information")
    @GetMapping
    public ResponseEntity<EventAttendanceResponseDto> getEventAttendees(@PathVariable UUID externalEventId) {
        return ResponseEntity.ok(attendanceService.getEventAttendees(externalEventId));
    }
}
