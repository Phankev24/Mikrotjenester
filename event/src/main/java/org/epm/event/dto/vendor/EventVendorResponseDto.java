package org.epm.event.dto.vendor;

import org.epm.event.dto.event.EventResponseDto;

import java.util.List;

public record EventVendorResponseDto(
        VendorResponseDto vendors,
        List<EventResponseDto> events
) {
}
