package org.epm.event.client;

import org.epm.event.dto.user.UserResponseDto;
import org.epm.event.dto.vendor.VendorResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class EventClientTest {

    private static final UUID USER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final UUID VENDOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private MockRestServiceServer server;
    private EventClient client;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = new RestTemplateBuilder(restTemplate -> server = MockRestServiceServer.bindTo(restTemplate).build());
        client = new EventClient(builder, "http://user.test", "http://vendor.test");
    }

    @Test
    void getUserByExternalIdCallsTheUserServiceAndMapsTheResponse() {
        server.expect(requestTo("http://user.test/api/user/external/" + USER_ID))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"id":1,"externalUserId":"%s","username":"ola","firstName":"Ola","lastName":"Nordmann",
                         "email":"ola@example.com","phoneNumber":"12345678","age":30,"gender":"Male"}
                        """.formatted(USER_ID), MediaType.APPLICATION_JSON));

        UserResponseDto user = client.getUserByExternalId(USER_ID);

        assertThat(user.username()).isEqualTo("ola");
        assertThat(user.externalUserId()).isEqualTo(USER_ID);
        server.verify();
    }

    @Test
    void getUsersReturnsTheListFromTheUserService() {
        server.expect(requestTo("http://user.test/api/user"))
                .andRespond(withSuccess("""
                        [{"id":1,"externalUserId":"%s","username":"ola"},{"id":2,"username":"kari"}]
                        """.formatted(USER_ID), MediaType.APPLICATION_JSON));

        List<UserResponseDto> users = client.getUsers();

        assertThat(users).extracting(UserResponseDto::username).containsExactly("ola", "kari");
        server.verify();
    }

    @Test
    void getVendorByExternalIdCallsTheVendorService() {
        server.expect(requestTo("http://vendor.test/api/vendors/" + VENDOR_ID))
                .andRespond(withSuccess("""
                        {"externalId":"%s","companyName":"Catering AS"}
                        """.formatted(VENDOR_ID), MediaType.APPLICATION_JSON));

        VendorResponseDto vendor = client.getVendorByExternalId(VENDOR_ID);

        assertThat(vendor.companyName()).isEqualTo("Catering AS");
        server.verify();
    }

    @Test
    void getUserByExternalIdReturnsNullWhenTheUserServiceAnswers404() {
        server.expect(requestTo("http://user.test/api/user/external/" + USER_ID))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.getUserByExternalId(USER_ID)).isNull();
        server.verify();
    }

    @Test
    void getVendorByExternalIdReturnsNullWhenTheVendorServiceAnswers404() {
        server.expect(requestTo("http://vendor.test/api/vendors/" + VENDOR_ID))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.getVendorByExternalId(VENDOR_ID)).isNull();
    }

    @Test
    void listCallsTurn404Into404() {
        server.expect(requestTo("http://user.test/api/user")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertStatus(() -> client.getUsers(), HttpStatus.NOT_FOUND);
    }

    @Test
    void otherClientErrorsKeepTheirStatus() {
        server.expect(requestTo("http://user.test/api/user/external/" + USER_ID))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertStatus(() -> client.getUserByExternalId(USER_ID), HttpStatus.FORBIDDEN);
    }

    @Test
    void aFailingServiceBecomes502() {
        server.expect(requestTo("http://user.test/api/user/external/" + USER_ID)).andRespond(withServerError());

        assertStatus(() -> client.getUserByExternalId(USER_ID), HttpStatus.BAD_GATEWAY);
    }

    @Test
    void anUnreachableServiceBecomes503() {
        server.expect(requestTo("http://vendor.test/api/vendors")).andRespond(request -> {
            throw new IOException("connection refused");
        });

        assertStatus(() -> client.getVendors(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    private static void assertStatus(ThrowingCallable call, HttpStatus status) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }

    @Test
    void getVendorsReturnsTheListFromTheVendorService() {
        server.expect(requestTo("http://vendor.test/api/vendors"))
                .andRespond(withSuccess("""
                        [{"externalId":"%s","companyName":"Catering AS"}]
                        """.formatted(VENDOR_ID), MediaType.APPLICATION_JSON));

        assertThat(client.getVendors()).extracting(VendorResponseDto::companyName).containsExactly("Catering AS");
        server.verify();
    }
}
