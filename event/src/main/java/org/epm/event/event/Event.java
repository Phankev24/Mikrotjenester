package org.epm.event.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private UUID vendorId;
    private String eventName;
    private String eventDescription;
    private int eventAttendance;
    private LocalDateTime eventDateTime;

    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;


    public Event(EventCategory eventCategory, LocalDateTime eventDateTime, int eventAttendance, String eventDescription, String eventName) {
        this.eventCategory = eventCategory;
        this.eventDateTime = eventDateTime;
        this.eventAttendance = eventAttendance;
        this.eventDescription = eventDescription;
        this.eventName = eventName;
    }
}
