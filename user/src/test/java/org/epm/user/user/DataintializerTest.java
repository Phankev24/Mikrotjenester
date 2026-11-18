package org.epm.user.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataintializerTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void seedsTheTwoDemoUsersWithHashedPasswordsAndFixedIds() throws Exception {
        when(passwordEncoder.encode("password")).thenReturn("hashed-password");

        new Dataintializer().initDatabase(userRepo, passwordEncoder).run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepo, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(User::getUsername).containsExactly("ola", "kari");
        assertThat(saved.getAllValues()).extracting(User::getPassword).containsOnly("hashed-password");
        assertThat(saved.getAllValues().get(0).getExternalUserId())
                .isEqualTo(UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe"));
        assertThat(saved.getAllValues().get(1).getExternalUserId())
                .isEqualTo(UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90"));
    }
}
