package org.epm.user.controller;

import org.epm.user.dto.UserCreateDto;
import org.epm.user.dto.UserResponseDto;
import org.epm.user.dto.UserEventCreateDto;
import org.epm.user.dto.UserUpdateDto;
import org.epm.user.dto.UserWithEventsDto;
import org.epm.user.event.EventDto;
import org.epm.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findByUserId(id));
    }

    @GetMapping("/external/{externalUserId}")
    public ResponseEntity<UserResponseDto> findByExternalUserId(@PathVariable UUID externalUserId) {
        return ResponseEntity.ok(userService.findByExternalUserId(externalUserId));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<UserWithEventsDto> getUserWithEvents(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserWithEvents(id));
    }

    @PostMapping("/{externalUserId}/events")
    public ResponseEntity<EventDto> createEvent(@PathVariable UUID externalUserId, @Valid @RequestBody UserEventCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createEventForUser(externalUserId, dto));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userCreateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto updatedUser = userService.updatedUser(id, userUpdateDto);

        return ResponseEntity.ok(updatedUser);
    }
}
