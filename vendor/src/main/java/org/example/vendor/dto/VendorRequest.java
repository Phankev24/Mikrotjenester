package org.example.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for POST (create vendor). Required fields are validated. */
public record VendorRequest(
        @NotBlank @Size(max = 100) String companyName,
        @NotBlank @Size(min = 8, max = 72) String password,   // 72 = BCrypt's limit
        @NotBlank @Email String email,
        String phone,
        String website,
        @Size(max = 1000) String description
) {
    // Records print every field in toString. This keeps the password out of logs.
    @Override
    public String toString() {
        return "VendorRequest[companyName=" + companyName + ", email=" + email + "]";
    }
}