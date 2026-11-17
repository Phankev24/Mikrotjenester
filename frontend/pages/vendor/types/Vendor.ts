export interface Vendor {
    externalId: string;
    companyName: string;
    email: string;
    phone: string;
    website: string;
    description: string;
}

/** POST /api/vendors */
export interface VendorRegisterRequest {
    companyName: string;
    email: string;
    password: string;
    phone?: string;
    website?: string;
    description?: string;
}

/** PATCH /api/vendors/{id}: alle felt er valgfrie. */
export type VendorPatchRequest = Partial<VendorRegisterRequest>;