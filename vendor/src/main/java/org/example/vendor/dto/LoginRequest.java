package org.example.vendor.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {
    // Holder passordet ute av logger.
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + "]";
    }
}