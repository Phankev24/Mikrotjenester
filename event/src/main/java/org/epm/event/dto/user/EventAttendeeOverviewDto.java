package org.epm.event.dto.user;

import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;

import java.util.List;

public record EventAttendeeOverviewDto(
        UserResponseDto attendee,
        List<EventAttendanceResponseDto> joinedEvents
) {
}
