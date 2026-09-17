package org.epm.user.user;

import org.epm.user.event.EventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String eventServiceBaseUrl;

    public UserClient(RestTemplate restTemplate,
                       @Value("${event-service.base-url}") String eventServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.eventServiceBaseUrl = eventServiceBaseUrl;
    }

    public List<EventDto> getAllEvents() {
        ResponseEntity<List<EventDto>> response = restTemplate.exchange(
                eventServiceBaseUrl + "/api/event",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EventDto>>() {}
        );
        return response.getBody();
    }
}
