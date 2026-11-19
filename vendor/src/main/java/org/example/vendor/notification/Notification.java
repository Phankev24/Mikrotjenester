package org.example.vendor.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID externalVendorId;

    @Column(nullable = false)
    private UUID externalEventId;

    private String eventName;

    private UUID attendeeUserId;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant joinedAt;
}
