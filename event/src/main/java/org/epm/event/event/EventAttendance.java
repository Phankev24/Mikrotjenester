package org.epm.event.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "event_attendance", uniqueConstraints = {@UniqueConstraint(name = "uk_event_user_attendance", columnNames = {"external_event_id", "external_user_id"})})
public class EventAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_event_id", nullable = false)
    private UUID externalEventId;

    @Column(name = "external_user_id", nullable = false)
    private UUID externalUserId;

    public EventAttendance(UUID externalEventId, UUID externalUserId) {
        this.externalEventId = externalEventId;
        this.externalUserId = externalUserId;
    }
}