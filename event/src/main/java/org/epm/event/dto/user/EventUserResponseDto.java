package org.epm.event.dto.user;

import org.epm.event.dto.event.EventResponseDto;

import java.util.List;

public record EventUserResponseDto(
        UserResponseDto users,
        List<EventResponseDto> events
) {
}
