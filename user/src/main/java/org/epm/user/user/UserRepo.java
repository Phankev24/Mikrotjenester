package org.epm.user.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByExternalUserId(UUID externalUserId);

    Optional<User> findByUsername(String username);

    Optional<User> findByGithubId(String githubId);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByUsername(String username);
}
