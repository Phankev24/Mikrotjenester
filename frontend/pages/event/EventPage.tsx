import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { eventApi } from './api.eventApi';
import { EventUserResponseDto, EventVendorResponseDto } from './types/event';
import { AdminDirectory } from './components/AdminDirectory';

type PortalMode = 'USER' | 'VENDOR' | 'ADMIN';

export default function EventPage() {
    const [mode, setMode] = useState<PortalMode>('ADMIN');
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
            setError('Failed to load data. Ensure Event (8080), User (8081), and Vendor (8082) services are running with CORS enabled.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDirectoryData();
    }, []);

    return (
        <div style={{ padding: '1.5rem', maxWidth: '1200px', margin: '0 auto', fontFamily: 'sans-serif' }}>
            <div style={{ marginBottom: '1rem' }}>
                <Link to="/">← Back to Home</Link>
            </div>

            <header
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '1rem',
                    backgroundColor: '#f4f4f4',
                    border: '1px solid #ddd',
                    marginBottom: '1.5rem',
                }}
            >
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button
                        onClick={() => setMode('USER')}
                        style={{ fontWeight: mode === 'USER' ? 'bold' : 'normal' }}
                    >
                        User Portal
                    </button>
                    <button
                        onClick={() => setMode('VENDOR')}
                        style={{ fontWeight: mode === 'VENDOR' ? 'bold' : 'normal' }}
                    >
                        Vendor Portal
                    </button>
                    <button
                        onClick={() => setMode('ADMIN')}
                        style={{ fontWeight: mode === 'ADMIN' ? 'bold' : 'normal' }}
                    >
                        Admin Directory
                    </button>
                </div>

                <div>
                    {mode === 'USER' && (
                        <label>
                            Acting as User:{' '}
                            <select
                                value={selectedUserId}
                                onChange={(e) => setSelectedUserId(e.target.value)}
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
                            >
                                {vendorsWithEvents.map(({ vendors: v }) => (
                                    <option key={v.externalId} value={v.externalId}>
                                        {v.companyName}
                                    </option>
                                ))}
                            </select>
                        </label>
                    )}

                    {mode === 'ADMIN' && <span>Mode: Global Service Inspector</span>}
                </div>
            </header>

            {error && (
                <div style={{ padding: '1rem', backgroundColor: '#ffe6e6', border: '1px solid red', marginBottom: '1rem' }}>
                    {error}
                </div>
            )}

            {loading ? (
                <p>Fetching microservices data...</p>
            ) : (
                <>
                    {mode === 'ADMIN' && (
                        <AdminDirectory
                            vendorsWithEvents={vendorsWithEvents}
                            usersWithEvents={usersWithEvents}
                        />
                    )}

                    {mode === 'USER' && (
                        <div>
                            <h2>User Portal</h2>
                            <p>Selected User UUID: <code>{selectedUserId || 'None'}</code></p>
                            <p><em>Ready for Step 2: Public Event Feed, Join/Leave, and Create User Event.</em></p>
                        </div>
                    )}

                    {mode === 'VENDOR' && (
                        <div>
                            <h2>Vendor Portal</h2>
                            <p>Selected Vendor UUID: <code>{selectedVendorId || 'None'}</code></p>
                            <p><em>Ready for Step 3: Hosted Vendor Events, Attendee Lists, and Create Vendor Event.</em></p>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}