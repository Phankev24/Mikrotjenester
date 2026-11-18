package org.epm.user.service;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        return new LoginResponseDto(tokenService.issueToken(user, "LOCAL"), userResponseMapper.toDTO(user));
    }

    public UserResponseDto me(UUID externalUserId) {
        return userRepo.findByExternalUserId(externalUserId)
                .map(userResponseMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }
}
