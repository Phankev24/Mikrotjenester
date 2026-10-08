package org.epm.event.client;

import org.epm.event.dto.VendorResponseDto;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EventClient {
    private final RestTemplate restTemplate;
    private final String vendorServiceUrl = "http://localhost:8082/api/vendors";

    public EventClient(RestTemplateBuilder builder){
        this.restTemplate = builder.build();
    }

    public List<VendorResponseDto> getAllVendors(){
        ResponseEntity<List<VendorResponseDto>> response = restTemplate.exchange(
                vendorServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VendorResponseDto>>() {}
        );
        return response.getBody();
    }



    public List<VendorResponseDto> getVendorsByVendorId(UUID vendorId){
        String url = vendorServiceUrl + "/vendor/" + vendorId;
        ResponseEntity<List<VendorResponseDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VendorResponseDto>>() {}
        );
        return response.getBody();
    }

}