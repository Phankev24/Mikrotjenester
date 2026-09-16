package org.epm.event.dto;

import lombok.Data;
import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;

@Data
public class EventDto {
    private Long eventId;
    private String eventName;
    private String eventDescription;
    private int eventAttendance;
    private LocalDateTime eventDateTime;
    private EventCategory eventCategory;
}
