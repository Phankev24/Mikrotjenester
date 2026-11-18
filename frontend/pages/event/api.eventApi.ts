import {
    EventAttendanceResponseDto,
    EventAttendeeOverviewDto,
    EventCreateDto, EventJoinedNotificationDto,
    EventOrganizerOverviewDto,
    EventResponseDto,
    EventUpdateDto,
    EventUserResponseDto,
    EventVendorOverviewDto,
    EventVendorResponseDto,
} from './types/event';

const BASE_URL = 'http://localhost:8100/api';

export const eventApi = {
    getVendorsWithEvents: async (): Promise<EventVendorResponseDto[]> => {
        const response = await fetch(`${BASE_URL}/client/event/vendorsWithEvents`);
        if (!response.ok) throw new Error('Failed to fetch vendors with events');
        return response.json();

    },

    getUserNotifications: async (externalUserId: string): Promise<EventJoinedNotificationDto[]> => {
        const response = await fetch(`${BASE_URL}/user/${externalUserId}/notifications`);
        if (!response.ok) return [];
        return response.json();
    },

    getVendorNotifications: async (externalVendorId: string): Promise<EventJoinedNotificationDto[]> => {
        const response = await fetch(`${BASE_URL}/vendors/${externalVendorId}/notifications`);
        if (!response.ok) return [];
        return response.json();
    },

    getUsersWithEvents: async (): Promise<EventUserResponseDto[]> => {
        const response = await fetch(`${BASE_URL}/client/event/usersWithEvents`);
        if (!response.ok) throw new Error('Failed to fetch users with events');
        return response.json();
    },

    getUserAttendingEvents: async (externalUserId: string): Promise<EventAttendeeOverviewDto> => {
        const response = await fetch(`${BASE_URL}/client/event/${externalUserId}/attendingEvents`);
        if (!response.ok) throw new Error('Failed to fetch attending events');
        return response.json();
    },

    getUserOrganizedEvents: async (externalUserId: string): Promise<EventOrganizerOverviewDto> => {
        const response = await fetch(`${BASE_URL}/client/event/${externalUserId}/organizedEvents`);
        if (!response.ok) throw new Error('Failed to fetch organized events');
        return response.json();
    },

    getAllEvents: async (): Promise<EventResponseDto[]> => {
        const response = await fetch(`${BASE_URL}/event`);
        if (!response.ok) throw new Error('Failed to fetch events');
        return response.json();
    },

    createUserEvent: async (externalUserId: string, data: EventCreateDto): Promise<EventResponseDto> => {
        const response = await fetch(`${BASE_URL}/event/user/${externalUserId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error('Failed to create user event');
        return response.json();
    },

    updateEvent: async (externalEventId: string, data: EventUpdateDto): Promise<EventResponseDto> => {
        const response = await fetch(`${BASE_URL}/event/${externalEventId}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error('Failed to update event');
        return response.json();
    },

    getEventAttendees: async (externalEventId: string): Promise<EventAttendanceResponseDto> => {
        const response = await fetch(`${BASE_URL}/event/${externalEventId}/attend`);
        if (!response.ok) throw new Error('Failed to fetch event attendees');
        return response.json();
    },

    registerAttendee: async (externalEventId: string, externalUserId: string): Promise<void> => {
        const response = await fetch(`${BASE_URL}/event/${externalEventId}/attend/${externalUserId}`, {
            method: 'POST',
        });
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Failed to register for event');
        }
    },

    removeAttendee: async (externalEventId: string, externalUserId: string): Promise<void> => {
        const response = await fetch(`${BASE_URL}/event/${externalEventId}/attend/${externalUserId}`, {
            method: 'DELETE',
        });
        if (!response.ok) throw new Error('Failed to remove attendance');
    },

    getVendorOrganizedEvents: async (externalVendorId: string): Promise<EventVendorOverviewDto> => {
        const response = await fetch(`${BASE_URL}/client/event/vendor/${externalVendorId}/organizedEvents`);
        if (!response.ok) throw new Error('Failed to fetch vendor organized events');
        return response.json();
    },

    createVendorEvent: async (externalVendorId: string, data: EventCreateDto): Promise<EventResponseDto> => {
        const response = await fetch(`${BASE_URL}/event/vendor/${externalVendorId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error('Failed to create vendor event');
        return response.json();
    },
};