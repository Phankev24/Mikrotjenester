package org.epm.event.client;


import org.epm.event.dto.VendorDto;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Service
public class EventClient {
    private final RestTemplate restTemplate;
    //TODO: Move url to application.yaml
    private final String vendorServiceUrl = "http://localhost:8082/api/vendors";

    public EventClient(RestTemplateBuilder builder){
        this.restTemplate = builder.build();
    }


    //Demo: Test RestTemplate, external (vendor service) GET request.
    public List<VendorDto> getVendors(){
        ResponseEntity<List<VendorDto>> response = restTemplate.exchange(
                vendorServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VendorDto>>() {}
        );
        return response.getBody();
    }


    public List<VendorDto> getVendorsByIds(List<UUID> vendorIds){
        if(vendorIds == null || vendorIds.isEmpty()){
            return List.of();
        }
        Set<UUID> wanted = new HashSet<>(vendorIds);

        return getVendors().stream()
                .filter(vendor -> wanted.contains(vendor.id()))
                .toList();
    }
}