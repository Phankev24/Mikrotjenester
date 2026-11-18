package org.epm.user.controller;

import jakarta.validation.Valid;
import org.epm.user.dto.LoginRequestDto;
import org.epm.user.dto.LoginResponseDto;
import org.epm.user.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.epm.user.dto.UserResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(@RequestHeader("X-User-Id") UUID externalUserId) {
        return ResponseEntity.ok(authService.me(externalUserId));
    }
}
