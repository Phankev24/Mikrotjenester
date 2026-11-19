package org.epm.user.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    List<Notification> findByExternalUserIdOrderByJoinedAtAscIdAsc(UUID externalUserId);

    boolean existsByExternalEventIdAndAttendeeUserIdAndJoinedAt(UUID externalEventId, UUID attendeeUserId, Instant joinedAt);
}
