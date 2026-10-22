package org.example.vendor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Vendor's only outgoing calls, both to the event service. All knowledge of event's API
 * lives here, so a change in event touches one file.
 * Both calls use the same path: /api/event/vendor/{externalVendorId}
 *  - POST creates an event with this vendor as organizer
 *  - GET  lists the events of this vendor
 * Errors: event down -> 503, event rejects or answers unexpectedly -> 502.
 */
@Component
public class EventClient {

    private static final String VENDOR_EVENTS = "/api/event/vendor/{externalVendorId}";

    private final RestClient restClient;

    public EventClient(@Value("${event.service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Posts a vendor event. Event sets the vendor as organizer and returns the created event. */
    public EventDto createVendorEvent(UUID vendorExternalId, EventPayload payload) {
        try {
            EventDto created = restClient.post()
                    .uri(VENDOR_EVENTS, vendorExternalId)
                    .body(payload)
                    .retrieve()
                    .body(EventDto.class);
            if (created == null || created.externalEventId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Event service returned no event");
            }
            return created;
        } catch (ResourceAccessException e) {
            throw unavailable();
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Event service rejected the request: " + e.getStatusCode()
                            + " " + e.getResponseBodyAsString());
        }
    }

    /**
     * Events posted by one vendor, read directly from event.
     * ASSUMPTION: event exposes GET on the same path and returns a JSON list.
     * A 404 is treated as "no events" so an unknown vendor does not become an error here.
     */
    public List<EventDto> getEventsByVendor(UUID vendorExternalId) {
        try {
            List<EventDto> events = restClient.get()
                    .uri(VENDOR_EVENTS, vendorExternalId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EventDto>>() {});
            return events != null ? events : List.of();
        } catch (HttpClientErrorException.NotFound e) {
            return List.of();
        } catch (ResourceAccessException e) {
            throw unavailable();
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Event service answered " + e.getStatusCode());
        }
    }

    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event service unavailable");
    }
}