package org.epm.user.dto;

import org.epm.user.event.EventDto;

import java.util.List;

public record UserWithEventsDto(UserResponseDto user, List<EventDto> events) {
}
