package org.epm.user.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.epm.user.service.OAuthUserService;
import org.epm.user.service.TokenService;
import org.epm.user.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserService oAuthUserService;
    private final TokenService tokenService;
    private final String clientUrl;

    public OAuthLoginSuccessHandler(OAuthUserService oAuthUserService,
                                    TokenService tokenService,
                                    @Value("${app.client-url:http://localhost:5173}") String clientUrl) {
        this.oAuthUserService = oAuthUserService;
        this.tokenService = tokenService;
        this.clientUrl = clientUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        String provider = oauth.getAuthorizedClientRegistrationId();

        User user = oAuthUserService.findOrCreate(provider, oauth.getPrincipal());
        String token = tokenService.issueToken(user, provider.toUpperCase());
        log.info("User {} ({}) logged in through {}", user.getUsername(), user.getExternalUserId(), provider);

        String target = UriComponentsBuilder.fromUriString(clientUrl)
                .path("/oauth-callback")
                .queryParam("token", token)
                .build()
                .toUriString();

        response.sendRedirect(target);
    }
}
