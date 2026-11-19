package org.example.vendor.dto;

import java.util.UUID;

public record LoginResponse(
        UUID externalId,
        String companyName,
        String email,
        String token
) {
}