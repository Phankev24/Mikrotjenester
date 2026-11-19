package org.example.vendor;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SeedMigrationTest {

    @Test
    void v003GivesEverySeededVendorTheBcryptHashOfTheDocumentedPassword() throws Exception {
        String sql = new String(new ClassPathResource("db/migration/V003__hash_seed_vendor_passwords.sql")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        Matcher hash = Pattern.compile("'(\\$2a\\$10\\$[^']+)'").matcher(sql);

        assertThat(hash.find()).isTrue();
        assertThat(new BCryptPasswordEncoder().matches("password123", hash.group(1))).isTrue();
        assertThat(sql).contains(
                "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000002",
                "00000000-0000-0000-0000-000000000003",
                "00000000-0000-0000-0000-000000000004",
                "11111111-1111-1111-1111-111111111111");
    }
}
