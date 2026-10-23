package org.epm.event.dto.event;

import org.epm.event.dto.vendor.VendorResponseDto;

import java.util.List;

public record EventVendorResponseDto(
        VendorResponseDto vendors,
        List<EventResponseDto> events
) {
}
