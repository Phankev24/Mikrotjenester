package org.epm.event.repository;

import org.epm.event.event.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, Long> {
    boolean existsByExternalEventIdAndExternalUserId(UUID externalEventId, UUID externalUserId);

    int countByExternalEventId(UUID externalEventId);

    List<EventAttendance> findByExternalEventId(UUID externalEventId);
}