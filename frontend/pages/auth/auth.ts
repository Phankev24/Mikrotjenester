import { UserResponseDto } from '../user/types/user';

const TOKEN_KEY = 'authToken';
const USER_KEY = 'authUser';

export const API_URL = 'http://localhost:8100/api';

export const auth = {
    getToken: (): string | null => localStorage.getItem(TOKEN_KEY),

    getUser: (): UserResponseDto | null => {
        const raw = localStorage.getItem(USER_KEY);
        return raw ? (JSON.parse(raw) as UserResponseDto) : null;
    },

    isLoggedIn: (): boolean => localStorage.getItem(TOKEN_KEY) !== null,

    setSession: (token: string, user: UserResponseDto) => {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    },

    logout: () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    },
};

export async function authFetch(input: string, init: RequestInit = {}): Promise<Response> {
    const headers = new Headers(init.headers);
    const token = auth.getToken();
    if (token) headers.set('Authorization', `Bearer ${token}`);

    const response = await fetch(input, { ...init, headers });
    if (response.status === 401) {
        auth.logout();
        window.location.href = '/login';
    }
    return response;
}
