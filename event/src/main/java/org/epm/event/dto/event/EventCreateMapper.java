package org.epm.event.dto.event;

import org.epm.event.event.Event;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class EventCreateMapper {
    public Event toEntity(EventCreateDto eventCreateDto, UUID externalUserId, UUID externalVendorId){
        if(eventCreateDto == null){
            return null;
        }

        UUID organizerId = externalUserId != null ? externalUserId : externalVendorId;
        UUID eventOrganizer = organizerId;

        return new Event(
                eventCreateDto.eventCategory(),
                LocalDateTime.now(),
                eventCreateDto.eventLocation(),
                eventCreateDto.eventAttendance(),
                eventCreateDto.eventDescription(),
                eventCreateDto.eventName(),
                eventOrganizer,
                externalVendorId,
                externalUserId,
                UUID.randomUUID()
        );
    }
}
