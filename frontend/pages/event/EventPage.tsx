import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { eventApi } from './api.eventApi';
import { EventUserResponseDto, EventVendorResponseDto } from './types/event';
import { AdminDirectory } from './components/AdminDirectory';
import { UserPortal } from './components/UserPortal';
import { VendorPortal } from './components/VendorPortal';

type PortalMode = 'USER' | 'VENDOR' | 'ADMIN';

export default function EventPage() {
    const [mode, setMode] = useState<PortalMode>('USER');
    const [vendorsWithEvents, setVendorsWithEvents] = useState<EventVendorResponseDto[]>([]);
    const [usersWithEvents, setUsersWithEvents] = useState<EventUserResponseDto[]>([]);

    const [selectedUserId, setSelectedUserId] = useState<string>('');
    const [selectedVendorId, setSelectedVendorId] = useState<string>('');

    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const loadDirectoryData = async () => {
        setLoading(true);
        setError(null);
        try {
            const [vendorsData, usersData] = await Promise.all([
                eventApi.getVendorsWithEvents(),
                eventApi.getUsersWithEvents(),
            ]);

            setVendorsWithEvents(vendorsData);
            setUsersWithEvents(usersData);

            if (usersData.length > 0 && !selectedUserId) {
                setSelectedUserId(usersData[0].user.externalUserId);
            }
            if (vendorsData.length > 0 && !selectedVendorId) {
                setSelectedVendorId(vendorsData[0].vendors.externalId);
            }
        } catch (err) {
            console.error(err);
            setError('Failed to load data. Ensure Event (8080), User (8081), and Vendor (8082) services are running.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDirectoryData();
    }, []);

    return (
        <div style={{ width: '100%', minHeight: '100vh', fontFamily: 'sans-serif', backgroundColor: '#fafafa' }}>
            {/* TOP NAVBAR ONLY */}
            <header
                style={{
                    position: 'sticky',
                    top: 0,
                    zIndex: 100,
                    width: '100%',
                    boxSizing: 'border-box',
                    backgroundColor: '#e5e7eb',
                    borderBottom: '1px solid #bbb',
                    padding: '0.75rem 2rem',
                }}
            >
                <div
                    style={{
                        maxWidth: '1200px',
                        margin: '0 auto',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        gap: '1rem',
                    }}
                >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
                        <Link to="/" style={{ textDecoration: 'none', color: '#111', fontWeight: 'bold' }}>
                            ← Home
                        </Link>

                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button
                                onClick={() => setMode('USER')}
                                style={{
                                    padding: '0.4rem 0.8rem',
                                    fontWeight: mode === 'USER' ? 'bold' : 'normal',
                                    backgroundColor: mode === 'USER' ? '#2563eb' : '#fff',
                                    color: mode === 'USER' ? '#fff' : '#111',
                                    border: '1px solid #999',
                                    cursor: 'pointer',
                                }}
                            >
                                User Portal
                            </button>
                            <button
                                onClick={() => setMode('VENDOR')}
                                style={{
                                    padding: '0.4rem 0.8rem',
                                    fontWeight: mode === 'VENDOR' ? 'bold' : 'normal',
                                    backgroundColor: mode === 'VENDOR' ? '#2563eb' : '#fff',
                                    color: mode === 'VENDOR' ? '#fff' : '#111',
                                    border: '1px solid #999',
                                    cursor: 'pointer',
                                }}
                            >
                                Vendor Portal
                            </button>
                            <button
                                onClick={() => setMode('ADMIN')}
                                style={{
                                    padding: '0.4rem 0.8rem',
                                    fontWeight: mode === 'ADMIN' ? 'bold' : 'normal',
                                    backgroundColor: mode === 'ADMIN' ? '#2563eb' : '#fff',
                                    color: mode === 'ADMIN' ? '#fff' : '#111',
                                    border: '1px solid #999',
                                    cursor: 'pointer',
                                }}
                            >
                                Admin Directory
                            </button>
                        </div>
                    </div>

                    <div>
                        {mode === 'USER' && (
                            <label>
                                Acting as User:{' '}
                                <select
                                    value={selectedUserId}
                                    onChange={(e) => setSelectedUserId(e.target.value)}
                                    style={{ padding: '0.3rem' }}
                                >
                                    {usersWithEvents.map(({ user }) => (
                                        <option key={user.externalUserId} value={user.externalUserId}>
                                            {user.firstName} {user.lastName} ({user.username})
                                        </option>
                                    ))}
                                </select>
                            </label>
                        )}

                        {mode === 'VENDOR' && (
                            <label>
                                Acting as Vendor:{' '}
                                <select
                                    value={selectedVendorId}
                                    onChange={(e) => setSelectedVendorId(e.target.value)}
                                    style={{ padding: '0.3rem' }}
                                >
                                    {vendorsWithEvents.map(({ vendors: v }) => (
                                        <option key={v.externalId} value={v.externalId}>
                                            {v.companyName}
                                        </option>
                                    ))}
                                </select>
                            </label>
                        )}

                        {mode === 'ADMIN' && <strong>Mode: Global Service Inspector</strong>}
                    </div>
                </div>
            </header>

            {/* MAIN CONTENT BELOW NAVBAR */}
            <main style={{ maxWidth: '1200px', margin: '0 auto', padding: '1.5rem 2rem' }}>
                {error && (
                    <div style={{ padding: '1rem', backgroundColor: '#ffe6e6', border: '1px solid red', marginBottom: '1rem' }}>
                        {error}
                    </div>
                )}

                {loading ? (
                    <p>Fetching microservices data...</p>
                ) : (
                    <>
                        {mode === 'USER' && (
                            <UserPortal
                                selectedUserId={selectedUserId}
                                onDataChanged={loadDirectoryData}
                            />
                        )}

                        {mode === 'VENDOR' && (
                            <VendorPortal
                                selectedVendorId={selectedVendorId}
                                onDataChanged={loadDirectoryData}
                            />
                        )}

                        {mode === 'ADMIN' && (
                            <AdminDirectory
                                vendorsWithEvents={vendorsWithEvents}
                                usersWithEvents={usersWithEvents}
                            />
                        )}
                    </>
                )}
            </main>
        </div>
    );
}