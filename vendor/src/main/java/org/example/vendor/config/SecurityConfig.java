package org.example.vendor.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            @Value("${GITHUB_ID:unset}") String githubId,
            @Value("${GITHUB_SECRET:unset}") String githubSecret,
            @Value("${GOOGLE_ID:unset}") String googleId,
            @Value("${GOOGLE_SECRET:unset}") String googleSecret,
            @Value("${app.oauth.redirect-base:http://localhost:8100}") String redirectBase) {

        if (googleId.isBlank() || "unset".equals(googleId) || googleSecret.isBlank() || "unset".equals(googleSecret)) {
            log.warn("Google OAuth login is not configured, set GOOGLE_ID and GOOGLE_SECRET to enable it");
        }
        if (githubId.isBlank() || "unset".equals(githubId) || githubSecret.isBlank() || "unset".equals(githubSecret)) {
            log.warn("GitHub OAuth login is not configured, set GITHUB_ID and GITHUB_SECRET to enable it");
        }

        ClientRegistration github = CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId(githubId.isBlank() ? "unset" : githubId)
                .clientSecret(githubSecret.isBlank() ? "unset" : githubSecret)
                .redirectUri(redirectBase + "/api/vendors/login/oauth2/code/github")
                .scope("read:user", "user:email")
                .build();

        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(googleId.isBlank() ? "unset" : googleId)
                .clientSecret(googleSecret.isBlank() ? "unset" : googleSecret)
                .redirectUri(redirectBase + "/api/vendors/login/oauth2/code/google")
                .scope("openid", "profile", "email")
                .build();

        return new InMemoryClientRegistrationRepository(github, google);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            OAuthLoginSuccessHandler successHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/vendors/oauth2/authorization"))
                        .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/vendors/login/oauth2/code/*"))
                        .successHandler(successHandler));

        return http.build();
    }
}