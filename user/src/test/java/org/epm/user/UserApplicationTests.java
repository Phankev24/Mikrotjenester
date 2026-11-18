package org.epm.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class UserApplicationTests extends IntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void flywayAppliedEveryMigration() {
        Integer applied = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class);

        assertThat(applied).isEqualTo(5);
    }
}
