package org.epm.user.user;

import org.epm.user.event.EventDto;

import java.util.List;

public record UserWithEventsDto(User user, List<EventDto> events) {
}
