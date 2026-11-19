package org.example.vendor.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Vendor's only outgoing call, to the event service: POST /api/event/vendor/{externalVendorId}
 * creates an event with this vendor as organizer.
 * Errors: event down -> 503, event rejects or answers unexpectedly -> 502.
 */
@Slf4j
@Component
public class EventClient {

    private static final String VENDOR_EVENTS = "/api/event/vendor/{externalVendorId}";

    private final RestClient restClient;

    public EventClient(@Value("${event.service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Posts a vendor event. Event sets the vendor as organizer and returns the created event. */
    public EventDto createVendorEvent(UUID vendorExternalId, EventPayload payload) {
        log.info("Calling event service: POST /api/event/vendor/{}", vendorExternalId);
        try {
            EventDto created = restClient.post()
                    .uri(VENDOR_EVENTS, vendorExternalId)
                    .body(payload)
                    .retrieve()
                    .body(EventDto.class);
            if (created == null || created.externalEventId() == null) {
                log.error("Event service returned no event for vendor {}", vendorExternalId);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Event service returned no event");
            }
            log.info("Event service created event {} for vendor {}", created.externalEventId(), vendorExternalId);
            return created;
        } catch (ResourceAccessException e) {
            log.error("Event service unreachable while posting event for vendor {}: {}", vendorExternalId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event service unavailable");
        } catch (RestClientResponseException e) {
            log.error("Event service answered {} while posting event for vendor {}", e.getStatusCode(), vendorExternalId);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Event service rejected the request: " + e.getStatusCode()
                            + " " + e.getResponseBodyAsString());
        }
    }
}