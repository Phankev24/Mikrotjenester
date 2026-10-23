package org.epm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record UserUpdateDto(
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String username,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String password,
        String firstName,
        String lastName,
        @Email String email,
        String phoneNumber,
        @Min(0) @Max(150) Integer age,
        String gender
) {

}
