package org.epm.event.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.epm.event.enumeration.EventCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @ElementCollection
    @CollectionTable(name = "event_vendor_ids", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "vendor_id")
    private List<UUID> vendorIds = new ArrayList<>();
}
