package org.epm.event.event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByExternalEventId(UUID externalEventId);
    List<Event> findByExternalUserId(UUID externalUserId);
    List<Event> findByExternalVendorId(UUID externalId);
}
