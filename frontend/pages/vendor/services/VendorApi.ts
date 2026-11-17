import { request } from '../../../src/api/api';
import type { EventRequest, VendorEvent } from '../types/Event';
import type { LoginRequest, LoginResponse } from '../types/Login';
import type { Vendor, VendorPatchRequest, VendorRegisterRequest } from '../types/Vendor';

export const vendorApi = {
    login: (body: LoginRequest) =>
        request<LoginResponse>('/api/auth/login',
            { method: 'POST', body: JSON.stringify(body) }, 'Innlogging feilet'),

    register: (body: VendorRegisterRequest) =>
        request<Vendor>('/api/vendors',
            { method: 'POST', body: JSON.stringify(body) }, 'Klarte ikke å opprette vendor'),

    getVendor: (id: string) =>
        request<Vendor>(`/api/vendors/${id}`, {}, 'Klarte ikke å hente profilen'),

    updateVendor: (id: string, body: VendorPatchRequest) =>
        request<Vendor>(`/api/vendors/${id}`,
            { method: 'PATCH', body: JSON.stringify(body) }, 'Klarte ikke å lagre'),

    postEvent: (id: string, body: EventRequest) =>
        request<VendorEvent>(`/api/vendors/${id}/events`,
            { method: 'POST', body: JSON.stringify(body) }, 'Klarte ikke å opprette eventet'),

    googleLogin: (idToken: string) =>
        request<LoginResponse>('/api/auth/google',
            { method: 'POST', body: JSON.stringify({ idToken }) }, 'Google-innlogging feilet'),
};