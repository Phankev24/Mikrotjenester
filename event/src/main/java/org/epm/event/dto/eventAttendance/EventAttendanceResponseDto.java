package org.epm.event.dto.eventAttendance;

import org.epm.event.dto.user.UserResponseDto;

import java.util.List;
import java.util.UUID;

public record EventAttendanceResponseDto(
        UUID externalEventId,
        String eventName,
        Integer maxCapacity,
        int currentAttendanceCount,
        int remainingCapacity,
        List<UserResponseDto> attendees
) {}