package org.epm.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class EventApplicationTests extends IntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void flywayAppliedEveryMigration() {
        Integer applied = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class);

        assertThat(applied).isEqualTo(2);
    }

    @Test
    void seedMigrationCreatesTheTwoDemoEvents() {
        Integer seeded = jdbcTemplate.queryForObject(
                "select count(*) from event where event_name in ('Tech Conference 2023', 'Summer Picnic')", Integer.class);

        assertThat(seeded).isEqualTo(2);
    }
}
