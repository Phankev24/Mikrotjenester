export interface LoginRequest {
    email: string;
    password: string;
}

/** Svar fra POST /api/auth/login. Ingen token ennå. */
export interface LoginResponse {
    externalId: string;
    companyName: string;
    email: string;
}