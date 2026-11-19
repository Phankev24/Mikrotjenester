package org.epm.user.service;

import lombok.extern.slf4j.Slf4j;
import org.epm.user.dto.LoginRequestDto;
import org.epm.user.dto.LoginResponseDto;
import org.epm.user.dto.UserResponseDto;
import java.util.UUID;
import org.epm.user.mapper.UserResponseMapper;
import org.epm.user.user.User;
import org.epm.user.user.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserResponseMapper userResponseMapper;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder,
                       TokenService tokenService, UserResponseMapper userResponseMapper) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.userResponseMapper = userResponseMapper;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepo.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> {
                    log.warn("Login failed for username {}", request.username());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
                });

        log.info("User logged in: {} ({})", user.getUsername(), user.getExternalUserId());
        return new LoginResponseDto(tokenService.issueToken(user, "LOCAL"), userResponseMapper.toDTO(user));
    }

    public UserResponseDto meFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("Profile requested without a bearer token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        return me(tokenService.subjectOf(authorization.substring("Bearer ".length())));
    }

    public UserResponseDto me(UUID externalUserId) {
        return userRepo.findByExternalUserId(externalUserId)
                .map(userResponseMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Profile requested for unknown user {}", externalUserId);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user");
                });
    }
}
