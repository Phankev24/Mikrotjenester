package org.epm.user.event;

public record EventCreateDto(
        String eventName,
        String eventDescription,
        EventCategory eventCategory,
        String eventLocation,
        int eventAttendance
) {
}
