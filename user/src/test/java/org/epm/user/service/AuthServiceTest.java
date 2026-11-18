package org.epm.user.service;

import org.epm.user.TestData;
import org.epm.user.dto.LoginRequestDto;
import org.epm.user.dto.LoginResponseDto;
import org.epm.user.dto.UserResponseDto;
import org.epm.user.mapper.UserResponseMapper;
import org.epm.user.user.User;
import org.epm.user.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final UUID OLA_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepo, passwordEncoder, tokenService, new UserResponseMapper());
    }

    @Test
    void loginReturnsTokenAndUserWhenPasswordMatches() {
        User ola = TestData.user(1L, OLA_ID, "ola");
        when(userRepo.findByUsername("ola")).thenReturn(Optional.of(ola));
        when(passwordEncoder.matches("password", "stored-hash")).thenReturn(true);
        when(tokenService.issueToken(ola, "LOCAL")).thenReturn("jwt-token");

        LoginResponseDto response = authService.login(new LoginRequestDto("ola", "password"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().username()).isEqualTo("ola");
        assertThat(response.user().externalUserId()).isEqualTo(OLA_ID);
    }

    @Test
    void loginRejectsAWrongPassword() {
        when(userRepo.findByUsername("ola")).thenReturn(Optional.of(TestData.user(1L, OLA_ID, "ola")));
        when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequestDto("ola", "wrong")))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(e.getReason()).isEqualTo("Invalid username or password");
                });
        verify(tokenService, never()).issueToken(any(), anyString());
    }

    @Test
    void loginRejectsAnUnknownUsernameWithTheSameMessage() {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequestDto("ghost", "password")))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(e.getReason()).isEqualTo("Invalid username or password");
                });
    }

    @Test
    void meReturnsTheCurrentUser() {
        when(userRepo.findByExternalUserId(OLA_ID)).thenReturn(Optional.of(TestData.user(1L, OLA_ID, "ola")));

        UserResponseDto me = authService.me(OLA_ID);

        assertThat(me.username()).isEqualTo("ola");
    }

    @Test
    void meFromAuthorizationResolvesTheUserFromTheBearerToken() {
        when(tokenService.subjectOf("good-token")).thenReturn(OLA_ID);
        when(userRepo.findByExternalUserId(OLA_ID)).thenReturn(Optional.of(TestData.user(1L, OLA_ID, "ola")));

        UserResponseDto me = authService.meFromAuthorization("Bearer good-token");

        assertThat(me.username()).isEqualTo("ola");
    }

    @Test
    void meFromAuthorizationRejectsAMissingOrNonBearerHeader() {
        assertThatThrownBy(() -> authService.meFromAuthorization(null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        assertThatThrownBy(() -> authService.meFromAuthorization("Basic abc"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verify(tokenService, never()).subjectOf(anyString());
    }

    @Test
    void meRejectsAnUnknownUserId() {
        when(userRepo.findByExternalUserId(OLA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me(OLA_ID))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}
