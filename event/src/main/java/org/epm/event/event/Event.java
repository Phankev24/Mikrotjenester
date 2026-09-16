package org.epm.event.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;
    private String eventName;
    private String eventDescription;
    private int eventAttendance;
    private LocalDateTime eventDateTime;
    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;


    public Event( String eventName, String eventDescription, int eventAttendance, LocalDateTime eventDateTime, EventCategory eventCategory){
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventAttendance = eventAttendance;
        this.eventDateTime = eventDateTime;
        this.eventCategory = eventCategory;
    }
}
