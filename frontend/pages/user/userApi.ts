import {
    EventDto,
    UserCreateDto,
    UserEventCreateDto,
    UserResponseDto,
    UserUpdateDto,
    UserWithEventsDto,
} from './types/user';
import { API_URL, authFetch } from '../auth/auth';

    const BASE_URL = `${API_URL}/user`;

    const json = { 'Content-Type': 'application/json' };

    export const userApi = {
    getUsers: async (): Promise<UserResponseDto[]> => {
        const response = await authFetch(BASE_URL);
        if (!response.ok) throw new Error('Failed to fetch users');
        return response.json();
    },

    getUserWithEvents: async (id: number): Promise<UserWithEventsDto> => {
        const response = await authFetch(`${BASE_URL}/${id}/events`);
        if (!response.ok) throw new Error('Failed to fetch user events');
        return response.json();
    },

    getAllEvents: async (): Promise<EventDto[]> => {
        const response = await authFetch(`${API_URL}/event`);
        if (!response.ok) throw new Error('Failed to fetch events');
        return response.json();
    },

    getAttendingEventIds: async (externalUserId: string): Promise<string[]> => {
        const response = await authFetch(`${API_URL}/client/event/${externalUserId}/attendingEvents`);
        if (!response.ok) throw new Error('Failed to fetch attended events');
        const data: { joinedEvents: { externalEventId: string }[] } = await response.json();
        return data.joinedEvents.map((e) => e.externalEventId);
    },

    createUser: async (dto: UserCreateDto): Promise<UserResponseDto> => {
        const response = await authFetch(BASE_URL, { method: 'POST', headers: json, body: JSON.stringify(dto) });
        if (!response.ok) throw new Error(`Failed to create user (${response.status}): ${await response.text()}`);
        return response.json();
    },

    updateUser: async (id: number, dto: UserUpdateDto): Promise<UserResponseDto> => {
        const response = await authFetch(`${BASE_URL}/${id}`, { method: 'PATCH', headers: json, body: JSON.stringify(dto) });
        if (!response.ok) throw new Error('Failed to update user');
        return response.json();
    },

    deleteUser: async (id: number): Promise<void> => {
        const response = await authFetch(`${BASE_URL}/${id}`, { method: 'DELETE' });
        if (!response.ok) throw new Error('Failed to delete user');
    },

    createEvent: async (externalUserId: string, dto: UserEventCreateDto): Promise<EventDto> => {
        const response = await authFetch(`${BASE_URL}/${externalUserId}/events`, {
            method: 'POST',
            headers: json,
            body: JSON.stringify(dto),
        });
        if (!response.ok) throw new Error('Failed to create event');
        return response.json();
    },
};
