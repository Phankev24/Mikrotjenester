package org.example.vendor.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String idToken) {
    // Keeps the token out of logs.
    @Override public String toString() { return "GoogleLoginRequest[***]"; }
}