package org.epm.event.repository;
import jakarta.persistence.LockModeType;
import org.epm.event.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByExternalEventId(UUID externalEventId);
    List<Event> findByExternalUserId(UUID externalUserId);
    List<Event> findByExternalVendorId(UUID externalId);
    List<Event> findByEventOrganizer(UUID eventOrganizer);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.externalEventId = :externalEventId")
    Optional<Event> findByExternalEventIdForUpdate(@Param("externalEventId") UUID externalEventId);
}
