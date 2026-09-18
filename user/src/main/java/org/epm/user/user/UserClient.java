package org.epm.user.user;

import org.epm.user.event.EventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private final String eventServiceUrl = "http://localhost:8080/api/event";
    private final String userServiceUrl = "http://localhost:8081/api/event";

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public List<EventDto> getAllEvents() {
        ResponseEntity<List<EventDto>> response = restTemplate.exchange(
                eventServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EventDto>>() {}
        );
        return response.getBody();
    }
}
