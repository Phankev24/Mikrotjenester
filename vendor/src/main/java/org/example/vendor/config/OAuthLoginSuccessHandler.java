package org.example.vendor.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.vendor.service.OAuthVendorService;
import org.example.vendor.service.TokenService;
import org.example.vendor.vendor.Vendor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthVendorService oAuthVendorService;
    private final TokenService tokenService;
    private final String clientUrl;

    public OAuthLoginSuccessHandler(OAuthVendorService oAuthVendorService,
                                    TokenService tokenService,
                                    @Value("${app.client-url:http://localhost:5173}") String clientUrl) {
        this.oAuthVendorService = oAuthVendorService;
        this.tokenService = tokenService;
        this.clientUrl = clientUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        String provider = oauth.getAuthorizedClientRegistrationId();

        Vendor vendor = oAuthVendorService.findOrCreate(provider, oauth.getPrincipal());
        String token = tokenService.issueToken(vendor, provider.toUpperCase());

        String target = UriComponentsBuilder.fromUriString(clientUrl)
                .path("/oauth-callback")
                .queryParam("token", token)
                .build()
                .toUriString();

        response.sendRedirect(target);
    }
}