import { EventUserResponseDto, EventVendorResponseDto } from './types/event';

const BASE_URL = 'http://localhost:8080/api';

export const eventApi = {
    getVendorsWithEvents: async (): Promise<EventVendorResponseDto[]> => {
        const response = await fetch(`${BASE_URL}/client/event/vendorsWithEvents`);
        if (!response.ok) throw new Error('Failed to fetch vendors with events');
        return response.json();
    },

    getUsersWithEvents: async (): Promise<EventUserResponseDto[]> => {
        const response = await fetch(`${BASE_URL}/client/event/usersWithEvents`);
        if (!response.ok) throw new Error('Failed to fetch users with events');
        return response.json();
    },
};