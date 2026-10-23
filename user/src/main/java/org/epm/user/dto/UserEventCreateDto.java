package org.epm.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.epm.user.event.EventCategory;

public record UserEventCreateDto(
        @NotBlank String eventName,
        String eventDescription,
        @NotNull EventCategory eventCategory,
        String eventLocation,
        @Min(0) int eventAttendance
) {
}
