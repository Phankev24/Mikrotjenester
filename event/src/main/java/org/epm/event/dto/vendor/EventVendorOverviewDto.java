package org.epm.event.dto.vendor;

import org.epm.event.dto.eventAttendance.EventAttendanceResponseDto;

import java.util.List;

public record EventVendorOverviewDto(
        VendorResponseDto vendor,
        List<EventAttendanceResponseDto> hostedEvents
) {
}
