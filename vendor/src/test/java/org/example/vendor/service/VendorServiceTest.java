package org.example.vendor.service;

import org.example.vendor.client.EventClient;
import org.example.vendor.client.EventDto;
import org.example.vendor.client.EventPayload;
import org.example.vendor.repository.VendorRepo;
import org.example.vendor.security.GoogleTokenVerifier;
import org.example.vendor.vendor.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

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
class VendorServiceTest {

    @Mock VendorRepo vendorRepo;
    @Mock EventClient eventClient;
    @Mock PasswordEncoder passwordEncoder;
    @Mock GoogleTokenVerifier googleVerifier;

    VendorService service;

    final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new VendorService(vendorRepo, eventClient, passwordEncoder, googleVerifier);
    }

    private Vendor existing() {
        return new Vendor(id, "Oslo Catering", "HASH", "post@oslo.no", "111", "https://oslo.no", "Mat");
    }

    private static void assertStatus(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, int status) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode().value()).isEqualTo(status));
    }

    // ---------- createVendor ----------

    @Test
    void createVendor_hashesPasswordAndSaves() {
        when(vendorRepo.existsByEmail("a@b.no")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASHED");
        when(vendorRepo.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

        Vendor result = service.createVendor(new Vendor("Firma", "secret123", "a@b.no", null, null, null));

        assertThat(result.getPassword()).isEqualTo("HASHED");
        ArgumentCaptor<Vendor> saved = ArgumentCaptor.forClass(Vendor.class);
        verify(vendorRepo).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isNotEqualTo("secret123");
    }

    @Test
    void createVendor_duplicateEmail_conflictAndNothingSaved() {
        when(vendorRepo.existsByEmail("a@b.no")).thenReturn(true);

        assertStatus(() -> service.createVendor(new Vendor("Firma", "secret123", "a@b.no", null, null, null)), 409);

        verify(vendorRepo, never()).save(any());
    }

    // ---------- reads ----------

    @Test
    void getAllVendors_returnsRepoContent() {
        when(vendorRepo.findAll()).thenReturn(List.of(existing()));

        assertThat(service.getAllVendors()).hasSize(1);
    }

    @Test
    void getVendor_found() {
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.of(existing()));

        assertThat(service.getVendor(id).getCompanyName()).isEqualTo("Oslo Catering");
    }

    @Test
    void getVendor_notFound_404() {
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.empty());

        assertStatus(() -> service.getVendor(id), 404);
    }

    // ---------- updateVendor ----------

    @Test
    void updateVendor_onlyNonNullFieldsChange() {
        Vendor vendor = existing();
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.of(vendor));
        when(vendorRepo.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

        Vendor patch = new Vendor();
        patch.setPhone("999");

        Vendor result = service.updateVendor(id, patch);

        assertThat(result.getPhone()).isEqualTo("999");
        assertThat(result.getCompanyName()).isEqualTo("Oslo Catering");
        assertThat(result.getEmail()).isEqualTo("post@oslo.no");
        assertThat(result.getPassword()).isEqualTo("HASH");   // not re-encoded
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateVendor_newPasswordIsEncoded() {
        Vendor vendor = existing();
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.of(vendor));
        when(passwordEncoder.encode("newpass123")).thenReturn("NEWHASH");
        when(vendorRepo.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

        Vendor patch = new Vendor();
        patch.setPassword("newpass123");

        assertThat(service.updateVendor(id, patch).getPassword()).isEqualTo("NEWHASH");
    }

    @Test
    void updateVendor_emailTakenByOther_conflict() {
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.of(existing()));
        when(vendorRepo.existsByEmail("taken@x.no")).thenReturn(true);

        Vendor patch = new Vendor();
        patch.setEmail("taken@x.no");

        assertStatus(() -> service.updateVendor(id, patch), 409);
        verify(vendorRepo, never()).save(any());
    }

    @Test
    void updateVendor_sameEmail_noUniquenessCheck() {
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.of(existing()));
        when(vendorRepo.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

        Vendor patch = new Vendor();
        patch.setEmail("post@oslo.no");

        service.updateVendor(id, patch);

        verify(vendorRepo, never()).existsByEmail(anyString());
    }

    @Test
    void updateVendor_notFound_404() {
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.empty());

        assertStatus(() -> service.updateVendor(id, new Vendor()), 404);
    }

    // ---------- login ----------

    @Test
    void login_validCredentials_returnsVendor() {
        when(vendorRepo.findByEmail("post@oslo.no")).thenReturn(Optional.of(existing()));
        when(passwordEncoder.matches("secret123", "HASH")).thenReturn(true);

        assertThat(service.login("post@oslo.no", "secret123").getExternalId()).isEqualTo(id);
    }

    @Test
    void login_wrongPassword_401() {
        when(vendorRepo.findByEmail("post@oslo.no")).thenReturn(Optional.of(existing()));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertStatus(() -> service.login("post@oslo.no", "wrong"), 401);
    }

    @Test
    void login_unknownEmail_401_withoutCheckingPassword() {
        when(vendorRepo.findByEmail("nobody@x.no")).thenReturn(Optional.empty());

        assertStatus(() -> service.login("nobody@x.no", "whatever"), 401);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // ---------- loginWithGoogle ----------

    @Test
    void loginWithGoogle_existingVendor_returnedWithoutSaving() {
        when(googleVerifier.verify("token")).thenReturn(new GoogleTokenVerifier.GoogleUser("post@oslo.no", "Whoever"));
        when(vendorRepo.findByEmail("post@oslo.no")).thenReturn(Optional.of(existing()));

        Vendor result = service.loginWithGoogle("token");

        assertThat(result.getExternalId()).isEqualTo(id);
        verify(vendorRepo, never()).save(any());
    }

    @Test
    void loginWithGoogle_newUser_createdWithGoogleNameAndRandomHashedPassword() {
        when(googleVerifier.verify("token")).thenReturn(new GoogleTokenVerifier.GoogleUser("new@x.no", "Anna"));
        when(vendorRepo.findByEmail("new@x.no")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("RANDOMHASH");
        when(vendorRepo.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

        Vendor result = service.loginWithGoogle("token");

        assertThat(result.getCompanyName()).isEqualTo("Anna");
        assertThat(result.getEmail()).isEqualTo("new@x.no");
        assertThat(result.getPassword()).isEqualTo("RANDOMHASH");
    }

    @Test
    void loginWithGoogle_blankName_usesEmailAsCompanyName() {
        when(googleVerifier.verify("token")).thenReturn(new GoogleTokenVerifier.GoogleUser("new@x.no", " "));
        when(vendorRepo.findByEmail("new@x.no")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("H");
        when(vendorRepo.save(any(Vendor.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(service.loginWithGoogle("token").getCompanyName()).isEqualTo("new@x.no");
    }

    @Test
    void loginWithGoogle_invalidToken_propagatesAndSavesNothing() {
        when(googleVerifier.verify("bad")).thenThrow(
                new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid Google token"));

        assertStatus(() -> service.loginWithGoogle("bad"), 401);
        verify(vendorRepo, never()).save(any());
    }

    // ---------- postEvent ----------

    @Test
    void postEvent_existingVendor_delegatesToEventClient() {
        EventPayload payload = new EventPayload("Fest", "Beskrivelse", "DINNER", "Oslo", 50);
        EventDto created = new EventDto(UUID.randomUUID(), id, "Fest", "Beskrivelse", "DINNER", "Oslo", null, 50);
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.of(existing()));
        when(eventClient.createVendorEvent(id, payload)).thenReturn(created);

        assertThat(service.postEvent(id, payload)).isSameAs(created);
    }

    @Test
    void postEvent_unknownVendor_404_andEventServiceNotCalled() {
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.empty());

        assertStatus(() -> service.postEvent(id, new EventPayload("x", null, "DINNER", null, 0)), 404);
        verify(eventClient, never()).createVendorEvent(any(), any());
    }

    // ---------- deleteVendor ----------

    @Test
    void deleteVendor_existing_deletes() {
        Vendor vendor = existing();
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.of(vendor));

        service.deleteVendor(id);

        verify(vendorRepo).delete(vendor);
    }

    @Test
    void deleteVendor_notFound_404_nothingDeleted() {
        when(vendorRepo.findByExternalId(id)).thenReturn(Optional.empty());

        assertStatus(() -> service.deleteVendor(id), 404);
        verify(vendorRepo, never()).delete(any());
    }
}