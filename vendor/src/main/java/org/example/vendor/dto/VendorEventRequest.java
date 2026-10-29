package org.example.vendor.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body for "vendor posts an event". The vendor id comes from the URL.
 * The @Schema examples are what Swagger pre-fills, so "Try it out" works without editing.
 */
public record VendorEventRequest(

        @Schema(example = "Summer buffet")
        @NotBlank String eventName,

        @Schema(example = "Buffet and finger food for 50 guests")
        String eventDescription,

        @Schema(example = "DINNER")
        @NotNull EventCatagory eventCategory,

        @Schema(example = "Oslo")
        String eventLocation,

        @Schema(example = "50")
        @Min(0) Integer eventAttendance
) {}