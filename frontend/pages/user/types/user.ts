export type EventCategory = 'SPORTS' | 'CONFERENCE' | 'DINNER' | 'SOCIAL' | 'MUSIC' | 'CONCERT' | 'PERFORMANCE';

export const EVENT_CATEGORIES: EventCategory[] = [
    'SPORTS',
    'CONFERENCE',
    'DINNER',
    'SOCIAL',
    'MUSIC',
    'CONCERT',
    'PERFORMANCE',
];

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

export interface UserCreateDto {
    username: string;
    password: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    phoneNumber?: string;
    age?: number;
    gender?: string;
}

export type UserUpdateDto = Partial<UserCreateDto>;

export interface UserEventCreateDto {
    eventName: string;
    eventDescription?: string;
    eventCategory: EventCategory;
    eventLocation?: string;
    eventAttendance: number;
}

export interface EventDto {
    internalEventId: number;
    externalEventId: string;
    externalUserId?: string | null;
    externalVendorId?: string | null;
    eventName: string;
    eventDescription: string;
    eventCategory: EventCategory;
    eventLocation: string;
    eventDateTime: string;
    eventAttendance: number;
}

export interface UserWithEventsDto {
    user: UserResponseDto;
    events: EventDto[];
}
