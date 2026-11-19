package org.example.vendor.controller;

import org.example.vendor.client.EventDto;
import org.example.vendor.dto.VendorRequestMapper;
import org.example.vendor.dto.VendorResponseMapper;
import org.example.vendor.kafka.VendorNotificationConsumer;
import org.example.vendor.service.VendorService;
import org.example.vendor.vendor.Vendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VendorControllerTest {

    @Mock VendorService vendorService;
    @Mock VendorNotificationConsumer notificationConsumer;

    MockMvc mvc;
    final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        VendorController controller = new VendorController(
                vendorService, new VendorRequestMapper(), new VendorResponseMapper(), notificationConsumer);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Vendor vendor() {
        return new Vendor(id, "Oslo Catering", "HASH", "post@oslo.no", "111", "https://oslo.no", "Mat");
    }

    private static final String VALID_VENDOR_JSON = """
            {"companyName":"Oslo Catering","email":"post@oslo.no","password":"secret123",
             "phone":"111","website":"https://oslo.no","description":"Mat"}""";

    // ---------- POST /api/vendors ----------

    @Test
    void createVendor_valid_201_andPasswordNeverReturned() throws Exception {
        when(vendorService.createVendor(any(Vendor.class))).thenReturn(vendor());

        mvc.perform(post("/api/vendors").contentType(MediaType.APPLICATION_JSON).content(VALID_VENDOR_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value(id.toString()))
                .andExpect(jsonPath("$.companyName").value("Oslo Catering"))
                .andExpect(jsonPath("$.email").value("post@oslo.no"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createVendor_missingCompanyName_400_serviceNotCalled() throws Exception {
        String json = """
                {"email":"post@oslo.no","password":"secret123"}""";

        mvc.perform(post("/api/vendors").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(vendorService);
    }

    @Test
    void createVendor_invalidEmail_400() throws Exception {
        String json = """
                {"companyName":"X","email":"not-an-email","password":"secret123"}""";

        mvc.perform(post("/api/vendors").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVendor_shortPassword_400() throws Exception {
        String json = """
                {"companyName":"X","email":"a@b.no","password":"short"}""";

        mvc.perform(post("/api/vendors").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVendor_duplicateEmail_409() throws Exception {
        when(vendorService.createVendor(any(Vendor.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered"));

        mvc.perform(post("/api/vendors").contentType(MediaType.APPLICATION_JSON).content(VALID_VENDOR_JSON))
                .andExpect(status().isConflict());
    }

    // ---------- GET ----------

    @Test
    void getAllVendors_returnsList() throws Exception {
        when(vendorService.getAllVendors()).thenReturn(List.of(vendor()));

        mvc.perform(get("/api/vendors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].externalId").value(id.toString()))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getAllVendors_empty_returnsEmptyArray() throws Exception {
        when(vendorService.getAllVendors()).thenReturn(List.of());

        mvc.perform(get("/api/vendors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getVendor_found() throws Exception {
        when(vendorService.getVendor(id)).thenReturn(vendor());

        mvc.perform(get("/api/vendors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Oslo Catering"));
    }

    @Test
    void getVendor_notFound_404() throws Exception {
        when(vendorService.getVendor(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mvc.perform(get("/api/vendors/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void getVendor_malformedUuid_400() throws Exception {
        mvc.perform(get("/api/vendors/not-a-uuid")).andExpect(status().isBadRequest());
    }

    // ---------- PATCH ----------

    @Test
    void updateVendor_partialBody_200_andNullFieldsPassedAsNull() throws Exception {
        when(vendorService.updateVendor(eq(id), any(Vendor.class))).thenReturn(vendor());

        mvc.perform(patch("/api/vendors/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"999\"}"))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<Vendor> captor = org.mockito.ArgumentCaptor.forClass(Vendor.class);
        verify(vendorService).updateVendor(eq(id), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getPhone()).isEqualTo("999");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getCompanyName()).isNull();
    }

    @Test
    void updateVendor_invalidEmail_400() throws Exception {
        mvc.perform(patch("/api/vendors/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nope\"}"))
                .andExpect(status().isBadRequest());

        verify(vendorService, never()).updateVendor(any(), any());
    }

    @Test
    void updateVendor_notFound_404() throws Exception {
        when(vendorService.updateVendor(eq(id), any(Vendor.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mvc.perform(patch("/api/vendors/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"1\"}"))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE ----------

    @Test
    void deleteVendor_204() throws Exception {
        mvc.perform(delete("/api/vendors/{id}", id)).andExpect(status().isNoContent());

        verify(vendorService).deleteVendor(id);
    }

    @Test
    void deleteVendor_notFound_404() throws Exception {
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(vendorService).deleteVendor(id);

        mvc.perform(delete("/api/vendors/{id}", id)).andExpect(status().isNotFound());
    }

    // ---------- POST /{id}/events ----------

    @Test
    void postEvent_valid_201_withEventFields() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(vendorService.postEvent(eq(id), any())).thenReturn(
                new EventDto(eventId, id, "Fest", "Beskrivelse", "DINNER", "Oslo", null, 50));

        String json = """
                {"eventName":"Fest","eventDescription":"Beskrivelse","eventCategory":"DINNER",
                 "eventLocation":"Oslo","eventAttendance":50}""";

        mvc.perform(post("/api/vendors/{id}/events", id).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalEventId").value(eventId.toString()))
                .andExpect(jsonPath("$.externalVendorId").value(id.toString()))
                .andExpect(jsonPath("$.eventCategory").value("DINNER"))
                .andExpect(jsonPath("$.eventAttendance").value(50));
    }

    @Test
    void postEvent_invalidCategory_400() throws Exception {
        String json = """
                {"eventName":"Fest","eventCategory":"string"}""";

        mvc.perform(post("/api/vendors/{id}/events", id).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(vendorService);
    }

    @Test
    void postEvent_missingName_400() throws Exception {
        mvc.perform(post("/api/vendors/{id}/events", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventCategory\":\"DINNER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postEvent_eventServiceDown_503() throws Exception {
        when(vendorService.postEvent(eq(id), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event service unavailable"));

        mvc.perform(post("/api/vendors/{id}/events", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventName\":\"Fest\",\"eventCategory\":\"DINNER\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    // ---------- notifications (kafka) ----------

    @Test
    void getNotifications_delegatesToConsumer() throws Exception {
        when(notificationConsumer.getNotificationsForVendor(id)).thenReturn(List.of());

        mvc.perform(get("/api/vendors/{id}/notifications", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(notificationConsumer).getNotificationsForVendor(id);
    }
}