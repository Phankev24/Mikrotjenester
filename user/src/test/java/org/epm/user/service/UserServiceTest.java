package org.epm.user.service;

import org.epm.user.TestData;
import org.epm.user.client.UserClient;
import org.epm.user.dto.UserCreateDto;
import org.epm.user.dto.UserResponseDto;
import org.epm.user.dto.UserUpdateDto;
import org.epm.user.dto.UserWithEventsDto;
import org.epm.user.event.EventCategory;
import org.epm.user.event.EventDto;
import org.epm.user.mapper.UserCreateMapper;
import org.epm.user.mapper.UserResponseMapper;
import org.epm.user.user.User;
import org.epm.user.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UUID OLA_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID KARI_ID = UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90");

    @Mock
    private UserRepo userRepo;
    @Mock
    private UserClient userClient;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepo, userClient, new UserResponseMapper(), new UserCreateMapper(), passwordEncoder);
    }

    @Test
    void findAllMapsEveryUser() {
        when(userRepo.findAll()).thenReturn(List.of(
                TestData.user(1L, OLA_ID, "ola"),
                TestData.user(2L, KARI_ID, "kari")));

        List<UserResponseDto> result = userService.findAll();

        assertThat(result).extracting(UserResponseDto::username).containsExactly("ola", "kari");
    }

    @Test
    void findByUserIdReturnsDto() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(TestData.user(1L, OLA_ID, "ola")));

        UserResponseDto result = userService.findByUserId(1L);

        assertThat(result.externalUserId()).isEqualTo(OLA_ID);
        assertThat(result.age()).isEqualTo(30);
    }

    @Test
    void findByUserIdThrows404WhenMissing() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUserId(99L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findByExternalUserIdReturnsDto() {
        when(userRepo.findByExternalUserId(OLA_ID)).thenReturn(Optional.of(TestData.user(1L, OLA_ID, "ola")));

        assertThat(userService.findByExternalUserId(OLA_ID).username()).isEqualTo("ola");
    }

    @Test
    void findByExternalUserIdThrows404WhenMissing() {
        when(userRepo.findByExternalUserId(OLA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByExternalUserId(OLA_ID))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createUserHashesThePasswordBeforeSaving() {
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto result = userService.createUser(
                new UserCreateDto("ola", "plain", "Ola", "Nordmann", "ola@example.com", "12345678", 30, "Male"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
        assertThat(saved.getValue().getUsername()).isEqualTo("ola");
        assertThat(result.username()).isEqualTo("ola");
        assertThat(result.email()).isEqualTo("ola@example.com");
    }

    @Test
    void createUserThrows409WhenTheUsernameIsTaken() {
        when(userRepo.existsByUsername("ola")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(
                new UserCreateDto("ola", "plain", "Ola", "Nordmann", "ola@example.com", "12345678", 30, "Male")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(userRepo, never()).save(any());
    }

    @Test
    void deleteByIdDeletesExistingUser() {
        when(userRepo.existsById(1L)).thenReturn(true);

        userService.deleteById(1L);

        verify(userRepo).deleteById(1L);
    }

    @Test
    void deleteByIdThrows404WhenMissing() {
        when(userRepo.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteById(1L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(userRepo, never()).deleteById(any());
    }

    @Test
    void getUserWithEventsKeepsOnlyEventsCreatedByThatUser() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(TestData.user(1L, OLA_ID, "ola")));
        EventDto olasEvent = event(OLA_ID, "Ola's party");
        EventDto karisEvent = event(KARI_ID, "Kari's party");
        EventDto vendorEvent = event(null, "Vendor event");
        when(userClient.getAllEvents()).thenReturn(List.of(olasEvent, karisEvent, vendorEvent));

        UserWithEventsDto result = userService.getUserWithEvents(1L);

        assertThat(result.user().username()).isEqualTo("ola");
        assertThat(result.events()).containsExactly(olasEvent);
    }

    @Test
    void updatedUserAppliesEveryProvidedField() {
        User existing = TestData.user(1L, OLA_ID, "ola");
        when(userRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto result = userService.updatedUser(1L, new UserUpdateDto(
                "ola2", "new-secret", "Ola2", "Hansen", "new@example.com", "99999999", 41, "Other"));

        assertThat(existing.getUsername()).isEqualTo("ola2");
        assertThat(existing.getPassword()).isEqualTo("new-hash");
        assertThat(existing.getFirstName()).isEqualTo("Ola2");
        assertThat(existing.getLastName()).isEqualTo("Hansen");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getPhoneNumber()).isEqualTo("99999999");
        assertThat(existing.getAge()).isEqualTo(41);
        assertThat(existing.getGender()).isEqualTo("Other");
        assertThat(result.username()).isEqualTo("ola2");
    }

    @Test
    void updatedUserLeavesFieldsUntouchedWhenTheyAreNull() {
        User existing = TestData.user(1L, OLA_ID, "ola");
        when(userRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updatedUser(1L, new UserUpdateDto(null, null, null, null, null, null, null, null));

        assertThat(existing.getUsername()).isEqualTo("ola");
        assertThat(existing.getPassword()).isEqualTo("stored-hash");
        assertThat(existing.getFirstName()).isEqualTo("Ola");
        assertThat(existing.getAge()).isEqualTo(30);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updatedUserThrows409WhenTheNewUsernameBelongsToSomeoneElse() {
        User existing = TestData.user(1L, OLA_ID, "ola");
        when(userRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepo.existsByUsername("kari")).thenReturn(true);

        assertThatThrownBy(() -> userService.updatedUser(1L,
                new UserUpdateDto("kari", null, null, null, null, null, null, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(existing.getUsername()).isEqualTo("ola");
        verify(userRepo, never()).save(any());
    }

    @Test
    void updatedUserThrows404WhenMissing() {
        when(userRepo.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatedUser(5L,
                new UserUpdateDto(null, null, null, null, null, null, null, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private static EventDto event(UUID externalUserId, String name) {
        return new EventDto(1L, UUID.randomUUID(), externalUserId, null, name, "desc",
                EventCategory.SOCIAL, "Oslo", LocalDateTime.of(2027, 1, 1, 18, 0), 20);
    }
}
