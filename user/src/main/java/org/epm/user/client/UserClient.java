package org.epm.user.client;

import lombok.extern.slf4j.Slf4j;
import org.epm.user.event.EventCreateDto;
import org.epm.user.event.EventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private final String eventServiceUrl;

    public UserClient(RestTemplate restTemplate, @Value("${event-service.base-url}") String eventServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.eventServiceUrl = eventServiceBaseUrl + "/api/event";
    }

    public List<EventDto> getAllEvents() {
        log.info("Calling event service: GET /api/event");
        try {
            ResponseEntity<List<EventDto>> response = restTemplate.exchange(
                    eventServiceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<EventDto>>() {
                    }
            );
            List<EventDto> events = response.getBody();
            return events != null ? events : List.of();
        } catch (RestClientException e) {
            log.error("Event service call failed: GET /api/event: {}", e.getMessage());
            throw e;
        }
    }

    public EventDto createEvent(UUID externalUserId, EventCreateDto eventCreateDto) {
        log.info("Calling event service: POST /api/event/user/{}", externalUserId);
        try {
            EventDto created = restTemplate.postForObject(eventServiceUrl + "/user/" + externalUserId, eventCreateDto, EventDto.class);
            log.info("Event service created event for user {}", externalUserId);
            return created;
        } catch (HttpClientErrorException e) {
            log.warn("Event service rejected event for user {} with {}", externalUserId, e.getStatusCode());
            throw new ResponseStatusException(e.getStatusCode(), "Event service rejected the request: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Event service failed to create event for user {} with {}", externalUserId, e.getStatusCode());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Event service failed to create the event");
        } catch (ResourceAccessException e) {
            log.error("Event service unreachable while creating event for user {}: {}", externalUserId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event service is unreachable");
        }
    }
}
