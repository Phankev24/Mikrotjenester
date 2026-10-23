package org.epm.event.event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event>findByExternalEventId(UUID externalEventId);
    List<Event> findByExternalVendorId(UUID externalId);
}
