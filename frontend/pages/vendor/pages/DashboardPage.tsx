import { useEffect, useState } from 'react';
import { ApiError } from '../../../src/api/api.ts';
import { vendorApi } from '../services/VendorApi.ts';
import ProfileForm from '../components/ProfileForm.tsx';
import type { LoginResponse } from '../types/Login';
import type { Vendor, VendorPatchRequest } from '../types/Vendor';

interface Props {
    session: LoginResponse;
    onLogout: () => void;
    onBackToVendors?: () => void; // Lagt til for tilbake-knapp
}

const card = { background: '#fff', padding: '1.5rem', borderRadius: 8, border: '1px solid #ddd', marginBottom: '1.5rem' } as const;

export default function DashboardPage({ session, onLogout, onBackToVendors }: Props) {
    const [vendor, setVendor] = useState<Vendor | null>(null);
    const [loadError, setLoadError] = useState<string | null>(null);

    useEffect(() => {
        vendorApi.getVendor(session.externalId)
            .then(setVendor)
            .catch((err: any) => {
                if (err instanceof ApiError && err.status === 404) onLogout();
                else setLoadError(err.message);
            });
    }, [session.externalId]);

    const saveProfile = async (patch: VendorPatchRequest) => {
        setVendor(await vendorApi.updateVendor(session.externalId, patch));
    };

    if (loadError) {
        return (
            <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
                <p style={{ color: '#b91c1c' }}>{loadError}</p>
                <button onClick={onLogout}>Til innlogging</button>
            </div>
        );
    }
    if (!vendor) return <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>Laster...</div>;

    return (
        <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: 800, margin: '0 auto' }}>
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <div>
                    <h1 style={{ margin: 0 }}>Hei, {vendor.companyName}</h1>
                    <small style={{ color: '#666' }}>ID: {vendor.externalId}</small>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    {onBackToVendors && (
                        <button onClick={onBackToVendors} style={{ background: '#4b5563', color: '#fff', padding: '0.6rem 1rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                            Tilbake
                        </button>
                    )}
                    <button onClick={onLogout} style={{ background: '#b91c1c', color: '#fff', padding: '0.6rem 1rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                        Logg ut
                    </button>
                </div>
            </header>

            <section style={card}>
                <h2 style={{ marginTop: 0 }}>Min profil</h2>
                <p><strong>E-post:</strong> {vendor.email}</p>
                <p><strong>Telefon:</strong> {vendor.phone || '-'}</p>
                <p><strong>Nettside:</strong> {vendor.website ? <a href={vendor.website} target="_blank" rel="noreferrer">{vendor.website}</a> : '-'}</p>
                <p><strong>Beskrivelse:</strong> {vendor.description || '-'}</p>
            </section>

            <section style={card}>
                <h2 style={{ marginTop: 0 }}>Rediger profil</h2>
                <ProfileForm vendor={vendor} onSave={saveProfile} />
            </section>

        </div>
    );
}