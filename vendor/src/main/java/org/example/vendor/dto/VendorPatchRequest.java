package org.example.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Body for PATCH. Everything is optional; a missing (null) field means "do not change". */
public record VendorPatchRequest(
        @Size(min = 1, max = 100) String companyName,
        @Size(min = 8, max = 72) String password,
        @Email String email,
        String phone,
        String website,
        @Size(max = 1000) String description
) {
    @Override
    public String toString() {
        return "VendorPatchRequest[companyName=" + companyName + ", email=" + email + "]";
    }
}