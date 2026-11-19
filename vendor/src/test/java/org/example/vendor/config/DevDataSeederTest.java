package org.example.vendor.config;

import org.example.vendor.repository.VendorRepo;
import org.example.vendor.vendor.Vendor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevDataSeederTest {

    @Mock
    private VendorRepo vendorRepo;
    @Captor
    private ArgumentCaptor<Iterable<Vendor>> saved;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void seedsFourVendorsWhoCanLogInWithTheDevPassword() {
        when(vendorRepo.count()).thenReturn(0L);

        new DevDataSeeder(vendorRepo, passwordEncoder).run();

        verify(vendorRepo).saveAll(saved.capture());
        assertThat(saved.getValue()).hasSize(4).allSatisfy(vendor -> {
            assertThat(vendor.getPassword()).isNotEqualTo("password123");
            assertThat(passwordEncoder.matches("password123", vendor.getPassword())).isTrue();
        });
    }

    @Test
    void doesNothingWhenVendorsAlreadyExist() {
        when(vendorRepo.count()).thenReturn(2L);

        new DevDataSeeder(vendorRepo, passwordEncoder).run();

        verify(vendorRepo, never()).saveAll(any());
    }
}
