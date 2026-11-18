package org.epm.user.dto;

public record LoginResponseDto(
        String token,
        UserResponseDto user
) {
}
