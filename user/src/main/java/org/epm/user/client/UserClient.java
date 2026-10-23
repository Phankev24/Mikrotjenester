package org.epm.user.client;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private final String eventServiceUrl;

    public UserClient(RestTemplate restTemplate, @Value("${event-service.base-url}") String eventServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.eventServiceUrl = eventServiceBaseUrl + "/api/event";
    }

    public List<EventDto> getAllEvents() {
        ResponseEntity<List<EventDto>> response = restTemplate.exchange(
                eventServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EventDto>>() {
                }
        );
        List<EventDto> events = response.getBody();
        return events != null ? events : List.of();
    }

    public EventDto createEvent(UUID externalUserId, EventCreateDto eventCreateDto) {
        try {
            return restTemplate.postForObject(eventServiceUrl + "/user/" + externalUserId, eventCreateDto, EventDto.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), "Event service rejected the request: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Event service failed to create the event");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Event service is unreachable");
        }
    }
}
