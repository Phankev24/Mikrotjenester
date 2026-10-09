package org.epm.event.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.epm.event.enumeration.EventCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalEventId;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID externalEventId;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID externalUserId;

    @Column(nullable = true, unique = true, updatable = true)
    private UUID externalVendorId;

    private String eventName;
    private String eventDescription;
    private int eventAttendance;
    private String eventLocation;
    private LocalDateTime eventDateTime;

    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;


    public Event(EventCategory eventCategory, LocalDateTime eventDateTime, String eventLocation, int eventAttendance, String eventDescription, String eventName, UUID externalVendorId, UUID externalUserId, UUID externalEventId) {
        this.eventCategory = eventCategory;
        this.eventDateTime = eventDateTime;
        this.eventLocation = eventLocation;
        this.eventAttendance = eventAttendance;
        this.eventDescription = eventDescription;
        this.eventName = eventName;
        this.externalVendorId = externalVendorId;
        this.externalUserId = externalUserId;
        this.externalEventId = externalEventId;
    }

    @PrePersist
    public void generateUuid(){
        if (this.externalEventId == null){
            this.externalEventId = UUID.randomUUID();
        }
        if(this.externalUserId == null){
            this.externalUserId = UUID.randomUUID();
        }
        //TODO: Auto generates datetime now but changes later for self input. cannot be null
        if(this.eventDateTime == null){
            this.eventDateTime = LocalDateTime.now();
        }
    }
}
