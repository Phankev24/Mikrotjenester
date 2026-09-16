package org.epm.event.client;

import org.epm.event.dto.EventDto;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class EventClient {
    private final RestTemplate restTemplate;
    private final String eventServiceUrl = "http://localhost:8080/api/event";

    public EventClient(RestTemplateBuilder builder){
        this.restTemplate = builder.build();
    }

    public List<EventDto> getEvents(){
        ResponseEntity<List<EventDto>> response = restTemplate.exchange(
                eventServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<EventDto>>() {}
        );
        return response.getBody();
    }
}
