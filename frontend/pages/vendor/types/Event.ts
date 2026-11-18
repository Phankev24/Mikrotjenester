// Må matche EventCategory i vendor (og event).
export const EVENT_CATEGORIES = [
    'PERFORMANCE', 'DINNER', 'MUSIC', 'CONCERT', 'CONFERENCE', 'SOCIAL', 'SPORTS',
] as const;

export type EventCategory = (typeof EVENT_CATEGORIES)[number];

export interface VendorEvent {
    externalEventId: string;
    externalVendorId: string;
    eventName: string;
    eventDescription: string;
    eventCategory: EventCategory;
    eventLocation: string;
    eventDateTime: string;
    eventAttendance: number;
}

/** POST /api/vendors/{id}/events */
export interface EventRequest {
    eventName: string;
    eventDescription: string;
    eventCategory: EventCategory;
    eventLocation: string;
    eventAttendance: number;
}