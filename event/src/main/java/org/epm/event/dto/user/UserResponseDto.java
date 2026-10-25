package org.epm.event.dto.user;

import java.util.UUID;

public record UserResponseDto(
        Long id,
        UUID externalUserId,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Integer age,
        String gender
) {
}
