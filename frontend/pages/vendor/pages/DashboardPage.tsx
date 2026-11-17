import { useEffect, useState } from 'react';
import { ApiError } from '../../../src/api/api.ts';
import { vendorApi } from '../services/VendorApi.ts';
import ProfileForm from '../components/ProfileForm.tsx';
import EventForm from '../components/EventForm.tsx';
import EventList from '../components/EventList.tsx';
import type { EventRequest, VendorEvent } from '../types/Event';
import type { LoginResponse } from '../types/Login';
import type { Vendor, VendorPatchRequest } from '../types/Vendor';

interface Props {
    session: LoginResponse;
    onLogout: () => void;
}

const card = { background: '#fff', padding: '1.5rem', borderRadius: 8, border: '1px solid #ddd', marginBottom: '1.5rem' } as const;

export default function DashboardPage({ session, onLogout }: Props) {
    const [vendor, setVendor] = useState<Vendor | null>(null);
    const [events, setEvents] = useState<VendorEvent[]>([]);
    const [loadError, setLoadError] = useState<string | null>(null);

    useEffect(() => {
        vendorApi.getVendor(session.externalId)
            .then(setVendor)
            .catch((err: any) => {
                // Kontoen finnes ikke lenger: send brukeren til innlogging
                if (err instanceof ApiError && err.status === 404) onLogout();
                else setLoadError(err.message);
            });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [session.externalId]);

    const saveProfile = async (patch: VendorPatchRequest) => {
        setVendor(await vendorApi.updateVendor(session.externalId, patch));
    };

    const createEvent = async (body: EventRequest) => {
        const created = await vendorApi.postEvent(session.externalId, body);
        // Vendor har ingen liste-endepunkt, så vi viser de eventene som er opprettet i denne økten
        setEvents(prev => [created, ...prev]);
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
                <button onClick={onLogout} style={{ background: '#6b7280', color: '#fff', padding: '0.6rem 1rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                    Logg ut
                </button>
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

            <section style={card}>
                <h2 style={{ marginTop: 0 }}>Opprett event</h2>
                <EventForm onSubmit={createEvent} />
                <EventList events={events} />
            </section>
        </div>
    );
}