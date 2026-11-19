package org.epm.event;

import org.epm.event.client.EventClient;
import org.epm.event.kafka.EventKafkaProducer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Every integration test extends this class, so they all share one Spring context and therefore
 * one Postgres container (started by {@link TestcontainersConfiguration}). The database is shared
 * too, so tests must use their own random UUIDs and not assume the tables are empty.
 * The calls to the user and vendor services and to Kafka are mocked.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestBase {

    @MockitoBean
    protected EventClient eventClient;

    @MockitoBean
    protected EventKafkaProducer eventKafkaProducer;
}
