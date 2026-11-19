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

        assertThat(applied).isEqualTo(7);
    }

    @Test
    void seedMigrationCreatesOlaAndKariWithFixedIds() {
        String olaId = jdbcTemplate.queryForObject(
                "select cast(external_user_id as varchar) from users where username = 'ola'", String.class);
        Integer kari = jdbcTemplate.queryForObject(
                "select count(*) from users where username = 'kari'", Integer.class);

        assertThat(olaId).isEqualTo("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
        assertThat(kari).isEqualTo(1);
    }
}
