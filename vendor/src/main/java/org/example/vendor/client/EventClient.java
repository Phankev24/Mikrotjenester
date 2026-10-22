package org.example.vendor.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Vendor's only outgoing calls, both to the event service.
 * Event's GET /api/event/{id} takes its internal Long id, which vendor must never use.
 * Vendor and event share the EXTERNAL UUID (externalEventId) instead.
 * All knowledge of event's API lives here, so a change in event touches one file.
 */
@Component
public class EventClient {

    private static final String EVENTS = "/api/event";

    private final RestClient restClient;

    public EventClient(@Value("${event.service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Does an event with this external UUID exist?
     * WORKAROUND: event has no "find by external UUID" endpoint, so we read the list
     * and filter. Replace with a direct GET once event adds one.
     * Event down -> 503, unexpected answer -> 502.
     */
    public boolean eventExists(UUID externalEventId) {
        try {
            List<EventRef> events = restClient.get().uri(EVENTS).retrieve()
                    .body(new ParameterizedTypeReference<List<EventRef>>() {});
            return events != null
                    && events.stream().anyMatch(e -> externalEventId.equals(e.externalEventId()));
        } catch (ResourceAccessException e) {
            throw unavailable();
        } catch (RestClientResponseException e) {
            throw badGateway("Event service answered " + e.getStatusCode());
        }
    }

    /** Creates an event and returns its external UUID, so vendor never invents an event id. */
    public UUID createEvent(EventPayload payload) {
        try {
            EventRef created = restClient.post().uri(EVENTS).body(payload)
                    .retrieve().body(EventRef.class);
            if (created == null || created.externalEventId() == null) {
                throw badGateway("Event service returned no externalEventId");
            }
            return created.externalEventId();
        } catch (ResourceAccessException e) {
            throw unavailable();
        } catch (RestClientResponseException e) {
            throw badGateway("Event service rejected the request: " + e.getStatusCode());
        }
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event service unavailable");
    }

    private ResponseStatusException badGateway(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }

    /** Only the external id is read from event's EventResponseDto; other fields are ignored. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record EventRef(UUID externalEventId) {}
}