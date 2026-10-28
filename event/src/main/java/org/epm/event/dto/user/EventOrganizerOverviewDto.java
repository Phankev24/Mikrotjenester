package org.epm.event.dto.user;

import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;

import java.util.List;

public record EventOrganizerOverviewDto(
        UserResponseDto organizer,
        List<EventAttendanceResponseDto> hostedEvents
) {
}
