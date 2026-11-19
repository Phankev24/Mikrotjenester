package org.epm.user.client;

import org.epm.user.event.EventCreateDto;
import org.epm.user.event.EventDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {

    private static final UUID USER_ID = UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe");
    private static final String EVENT_JSON = """
            {"internalEventId":1,"externalEventId":"22222222-2222-2222-2222-222222222222",
             "externalUserId":"a7f3bf47-f373-4cdf-b81c-b0475305b7fe","externalVendorId":null,
             "eventName":"Summer Picnic","eventDescription":"Community Picnic","eventCategory":"SOCIAL",
             "eventLocation":"City Park","eventDateTime":"2027-01-01T18:00:00","eventAttendance":50}
            """;

    private MockRestServiceServer server;
    private UserClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new UserClient(restTemplate, "http://event.test");
    }

    @Test
    void getAllEventsReturnsTheEventsFromTheEventService() {
        server.expect(requestTo("http://event.test/api/event"))
                .andExpect(method(GET))
                .andRespond(withSuccess("[" + EVENT_JSON + "]", MediaType.APPLICATION_JSON));

        List<EventDto> events = client.getAllEvents();

        assertThat(events).extracting(EventDto::eventName).containsExactly("Summer Picnic");
        server.verify();
    }

    @Test
    void getAllEventsReturnsAnEmptyListWhenTheBodyIsEmpty() {
        server.expect(requestTo("http://event.test/api/event")).andRespond(withSuccess());

        assertThat(client.getAllEvents()).isEmpty();
    }

    @Test
    void getAllEventsRethrowsWhenTheEventServiceFails() {
        server.expect(requestTo("http://event.test/api/event")).andRespond(withServerError());

        assertThatThrownBy(() -> client.getAllEvents()).isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void createEventPostsToTheEventServiceForThatUser() {
        server.expect(requestTo("http://event.test/api/event/user/" + USER_ID))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.CREATED).body(EVENT_JSON).contentType(MediaType.APPLICATION_JSON));

        EventDto created = client.createEvent(USER_ID, createDto());

        assertThat(created.eventName()).isEqualTo("Summer Picnic");
        assertThat(created.externalUserId()).isEqualTo(USER_ID);
        server.verify();
    }

    @Test
    void createEventPassesOnARejectionFromTheEventService() {
        server.expect(requestTo("http://event.test/api/event/user/" + USER_ID)).andRespond(withBadRequest());

        assertThatThrownBy(() -> client.createEvent(USER_ID, createDto()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createEventTurnsAnEventServiceFailureInto502() {
        server.expect(requestTo("http://event.test/api/event/user/" + USER_ID)).andRespond(withServerError());

        assertThatThrownBy(() -> client.createEvent(USER_ID, createDto()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void createEventTurnsAnUnreachableEventServiceInto503() {
        server.expect(requestTo("http://event.test/api/event/user/" + USER_ID)).andRespond(request -> {
            throw new IOException("connection refused");
        });

        assertThatThrownBy(() -> client.createEvent(USER_ID, createDto()))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    private static EventCreateDto createDto() {
        return new EventCreateDto("Summer Picnic", "Community Picnic", org.epm.user.event.EventCategory.SOCIAL, "City Park", 50);
    }
}
