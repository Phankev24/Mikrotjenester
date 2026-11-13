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