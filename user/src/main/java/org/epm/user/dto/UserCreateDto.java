package org.epm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UserCreateDto(
        @NotBlank String username,
        @NotBlank String password,
        String firstName,
        String lastName,
        @Email String email,
        String phoneNumber,
        @Min(0) @Max(150) Integer age,
        String gender
) {
}
