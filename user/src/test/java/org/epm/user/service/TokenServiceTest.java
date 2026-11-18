package org.epm.user.service;

import org.epm.user.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-unit-test";
    private static final UUID OLA_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");

    private final TokenService tokenService = new TokenService(SECRET, 60);

    @Test
    void issuedTokenCanBeReadBackToTheSameUserId() {
        String token = tokenService.issueToken(TestData.user(1L, OLA_ID, "ola"), "LOCAL");

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(tokenService.subjectOf(token)).isEqualTo(OLA_ID);
    }

    @Test
    void garbageTokenIsRejected() {
        assertThatThrownBy(() -> tokenService.subjectOf("garbage"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
