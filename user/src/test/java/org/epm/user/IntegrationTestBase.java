package org.epm.user;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Every integration test extends this class, so they all share one Spring context and therefore
 * one Postgres container and one Kafka container (started by {@link TestcontainersConfiguration}).
 * The database is shared too, so tests must use unique usernames.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestBase {
}
