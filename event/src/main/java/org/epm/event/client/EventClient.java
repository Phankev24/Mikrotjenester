package org.epm.event.client;

import lombok.extern.slf4j.Slf4j;
import org.epm.event.dto.user.UserResponseDto;
import org.epm.event.dto.vendor.VendorResponseDto;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
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

@Slf4j
@Service
public class EventClient {
    private final RestTemplate restTemplate;
    private String vendorServiceUrl = "http://localhost:8082/api/vendors";
    private String userServiceUrl = "http://localhost:8081/api/user";

    public EventClient(
            RestTemplateBuilder builder,
            @Value("${services.user.url}") String userBaseUrl,
            @Value("${services.vendor.url}") String vendorBaseUrl) {
        this.restTemplate = builder.build();
        this.userServiceUrl = userBaseUrl + "/api/user";
        this.vendorServiceUrl = vendorBaseUrl + "/api/vendors";
    }

    public List<VendorResponseDto> getVendors(){
        return call("vendor service GET /api/vendors", false, () -> restTemplate.exchange(
                vendorServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VendorResponseDto>>() {}
        ));
    }

    public VendorResponseDto getVendorByExternalId(UUID externalId){
        String url = vendorServiceUrl + "/" + externalId;
        return call("vendor service GET /api/vendors/" + externalId, true, () -> restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                VendorResponseDto.class
        ));
    }


    public List<UserResponseDto> getUsers(){
        return call("user service GET /api/user", false, () -> restTemplate.exchange(
                userServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UserResponseDto>>() {}
        ));
    }

    public UserResponseDto getUserByExternalId(UUID externalUserId){
        String url = userServiceUrl + "/external/" + externalUserId;
        return call("user service GET /api/user/external/" + externalUserId, true, () -> restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                UserResponseDto.class
        ));
    }

    private <T> T call(String description, boolean missingIsNull, Supplier<ResponseEntity<T>> request) {
        log.info("Calling {}", description);
        try {
            return request.get().getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("{} answered 404", description);
            if (missingIsNull) {
                return null;
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found in another service");
        } catch (HttpClientErrorException e) {
            log.warn("{} answered {}", description, e.getStatusCode());
            throw new ResponseStatusException(e.getStatusCode(), "Another service rejected the request");
        } catch (HttpServerErrorException e) {
            log.error("{} failed with {}", description, e.getStatusCode());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Another service failed to answer");
        } catch (ResourceAccessException e) {
            log.error("{} is unreachable: {}", description, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Another service is unreachable");
        }
    }
}
