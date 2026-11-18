export const API = 'http://localhost:8082';

/** Feil med HTTP-status, slik at sidene kan skille mellom f.eks. 401 og 404. */
export class ApiError extends Error {
    status: number;
    constructor(message: string, status: number) {
        super(message);
        this.status = status;
    }
}

/** Henter feilmeldingen fra serverens JSON-svar (RFC 7807 "detail"), ellers reservetekst. */
async function errorText(res: Response, fallback: string): Promise<string> {
    try {
        const body = await res.json();
        return body.detail || body.message || fallback;
    } catch {
        return fallback;
    }
}

/** Felles fetch-hjelper. Kaster ApiError ved feilstatus. */
export async function request<T>(path: string, options: RequestInit = {}, fallback = 'Noe gikk galt'): Promise<T> {
    let res: Response;
    try {
        res = await fetch(`${API}${path}`, {
            ...options,
            headers: { 'Content-Type': 'application/json', ...options.headers },
        });
    } catch {
        // Serveren svarer ikke, eller CORS blokkerer
        throw new ApiError('Får ikke kontakt med serveren', 0);
    }
    if (!res.ok) throw new ApiError(await errorText(res, fallback), res.status);
    if (res.status === 204) return undefined as T;
    return res.json() as Promise<T>;
}