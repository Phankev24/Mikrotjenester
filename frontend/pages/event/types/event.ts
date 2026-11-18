export interface EventResponseDto {
    internalEventId: number;
    externalEventId: string;
    externalUserId?: string | null;
    externalVendorId?: string | null;
    eventOrganizer: string;
    eventName: string;
    eventDescription: string;
    eventCategory: string;
    eventLocation: string;
    eventDateTime: string;
    eventAttendance: number;
}

export interface EventCreateDto {
    eventName: string;
    eventDescription: string;
    eventCategory: string;
    eventLocation: string;
    eventAttendance: number;
    eventDateTime?: string;
}

export interface EventUpdateDto {
    eventName?: string;
    eventDescription?: string;
    eventAttendance?: number;
    eventLocation?: string;
    eventDateTime?: string;
    eventCategory?: string;
}

export interface UserResponseDto {
    id: number;
    externalUserId: string;
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
    age: number;
    gender: string;
}

export interface VendorResponseDto {
    externalId: string;
    eventId?: string;
    companyName: string;
    email: string;
    phone: string;
    website: string;
    description: string;
}

export interface EventVendorResponseDto {
    vendors: VendorResponseDto;
    events: EventResponseDto[];
}

export interface EventUserResponseDto {
    user: UserResponseDto;
    events: EventResponseDto[];
}

export interface EventAttendanceResponseDto {
    externalEventId: string;
    eventName: string;
    maxCapacity: number;
    currentAttendanceCount: number;
    remainingCapacity: number;
    attendees: UserResponseDto[];
}

export interface EventAttendeeOverviewDto {
    attendee: UserResponseDto;
    joinedEvents: EventAttendanceResponseDto[];
}

export interface EventOrganizerOverviewDto {
    organizer: UserResponseDto;
    hostedEvents: EventAttendanceResponseDto[];
}

export interface EventVendorOverviewDto {
    vendor: VendorResponseDto;
    hostedEvents: EventAttendanceResponseDto[];
}

export interface EventJoinedNotificationDto {
    externalEventId: string;
    eventName: string;
    attendeeUserId: string;
    message: string;
    joinedAt: string;
}