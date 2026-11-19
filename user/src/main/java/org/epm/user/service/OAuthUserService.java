package org.epm.user.service;

import lombok.extern.slf4j.Slf4j;
import org.epm.user.user.User;
import org.epm.user.user.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OAuthUserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public OAuthUserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User findOrCreate(String provider, OAuth2User principal) {
        return switch (provider) {
            case "github" -> findOrCreateGithubUser(principal);
            case "google" -> findOrCreateGoogleUser(principal);
            default -> {
                log.warn("OAuth login rejected, unsupported provider {}", provider);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + provider);
            }
        };
    }

    private User findOrCreateGithubUser(OAuth2User principal) {
        String githubId = String.valueOf(principal.<Object>getAttribute("id"));

        return userRepo.findByGithubId(githubId).orElseGet(() -> {
            String login = principal.getAttribute("login");
            String name = principal.getAttribute("name");

            User user = newOAuthUser(login, principal.getAttribute("email"));
            user.setGithubId(githubId);

            String displayName = (name == null || name.isBlank()) ? login : name;
            int space = displayName.indexOf(' ');
            user.setFirstName(space > 0 ? displayName.substring(0, space) : displayName);
            user.setLastName(space > 0 ? displayName.substring(space + 1) : null);

            log.info("Creating user {} from GitHub login", user.getUsername());
            return userRepo.save(user);
        });
    }

    private User findOrCreateGoogleUser(OAuth2User principal) {
        String googleId = principal.getAttribute("sub");

        return userRepo.findByGoogleId(googleId).orElseGet(() -> {
            String email = principal.getAttribute("email");
            String givenName = principal.getAttribute("given_name");
            String familyName = principal.getAttribute("family_name");

            String base = email != null && email.contains("@")
                    ? email.substring(0, email.indexOf('@'))
                    : "google-user";

            User user = newOAuthUser(base, email);
            user.setGoogleId(googleId);
            user.setFirstName(givenName);
            user.setLastName(familyName);

            log.info("Creating user {} from Google login", user.getUsername());
            return userRepo.save(user);
        });
    }

    private User newOAuthUser(String usernameBase, String email) {
        User user = new User();
        user.setUsername(uniqueUsername(usernameBase));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEmail(email);
        return user;
    }

    private String uniqueUsername(String base) {
        String candidate = base;
        while (userRepo.existsByUsername(candidate)) {
            candidate = base + ThreadLocalRandom.current().nextInt(1000, 10000);
        }
        return candidate;
    }
}
