import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { eventApi } from './api.eventApi';
import { useAuth } from '../auth/authContext';
import { EventUserResponseDto, EventVendorResponseDto } from './types/event';
import { AdminDirectory } from './components/AdminDirectory';
import { UserPortal } from './components/UserPortal';
import { VendorPortal } from './components/VendorPortal';

type PortalMode = 'USER' | 'VENDOR' | 'ADMIN';

export default function EventPage() {
    const navigate = useNavigate();
    const { user: loggedInUser, logout } = useAuth();

    const storedRole = localStorage.getItem('epm_role');
    const storedVendorId = localStorage.getItem('epm_vendor_id') || '';

    const [isSensorMode, setIsSensorMode] = useState<boolean>(storedRole === 'SENSOR');
    const [isVendorSession, setIsVendorSession] = useState<boolean>(
        storedRole === 'VENDOR' && Boolean(storedVendorId)
    );
    const [mode, setMode] = useState<PortalMode>(
        storedRole === 'VENDOR' && storedVendorId ? 'VENDOR' : 'USER'
    );

    const [vendorsWithEvents, setVendorsWithEvents] = useState<EventVendorResponseDto[]>([]);
    const [usersWithEvents, setUsersWithEvents] = useState<EventUserResponseDto[]>([]);

    // Dropdown-valg som kun brukes i Sensor-modus
    const [sensorUserId, setSensorUserId] = useState<string>('');
    const [sensorVendorId, setSensorVendorId] = useState<string>('');

    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const activeUserId = isSensorMode
        ? sensorUserId
        : (loggedInUser?.externalUserId ?? '');

    const activeVendorId = isSensorMode
        ? sensorVendorId
        : storedVendorId;

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

            if (usersData.length > 0 && !sensorUserId) {
                setSensorUserId(usersData[0].user.externalUserId);
            }
            if (vendorsData.length > 0 && !sensorVendorId) {
                setSensorVendorId(vendorsData[0].vendors.externalId);
            }
        } catch (err) {
            console.error(err);
            setError('Failed to load data. Ensure Event, User, and Vendor services are running.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDirectoryData();
    }, []);

    const enableSensorMode = () => {
        localStorage.setItem('epm_role', 'SENSOR');
        setIsSensorMode(true);
    };

    const exitSensorMode = () => {
        localStorage.removeItem('epm_role');
        setIsSensorMode(false);
        if (storedVendorId) {
            localStorage.setItem('epm_role', 'VENDOR');
            setIsVendorSession(true);
            setMode('VENDOR');
        } else {
            setIsVendorSession(false);
            setMode('USER');
        }
    };

    const handleUserLogout = () => {
        logout();
        navigate('/login');
    };

    const handleVendorLogout = () => {
        localStorage.removeItem('epm_role');
        localStorage.removeItem('epm_vendor_id');
        setIsVendorSession(false);
        navigate('/vendor');
    };

    const currentVendor = vendorsWithEvents.find((v) => v.vendors.externalId === activeVendorId)?.vendors;

    return (
        <div style={{ width: '100%', minHeight: '100vh', fontFamily: 'sans-serif', backgroundColor: '#fafafa' }}>
            {/* TOP NAVBAR */}
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
                            &larr; Home
                        </Link>

                        {isSensorMode ? (
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
                        ) : isVendorSession ? (
                            <strong>Vendor Event Portal</strong>
                        ) : (
                            <strong>Events &amp; User Portal</strong>
                        )}
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                        {isSensorMode ? (
                            <>
                                {mode === 'USER' && (
                                    <label>
                                        Acting as User:{' '}
                                        <select
                                            value={sensorUserId}
                                            onChange={(e) => setSensorUserId(e.target.value)}
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
                                            value={sensorVendorId}
                                            onChange={(e) => setSensorVendorId(e.target.value)}
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

                                <button onClick={exitSensorMode} style={{ padding: '0.3rem 0.65rem', cursor: 'pointer' }}>
                                    Exit Sensor Mode
                                </button>
                            </>
                        ) : isVendorSession ? (
                            <>
                                <span style={{ fontSize: '0.9rem' }}>
                                    Vendor: <strong>{currentVendor?.companyName || activeVendorId}</strong>
                                </span>
                                <Link to="/vendor" style={{ fontSize: '0.85rem' }}>
                                    Vendor Dashboard
                                </Link>
                                <button onClick={handleVendorLogout} style={{ padding: '0.3rem 0.65rem', cursor: 'pointer' }}>
                                    Logg ut
                                </button>
                                <button
                                    onClick={enableSensorMode}
                                    style={{
                                        padding: '0.35rem 0.7rem',
                                        fontSize: '0.8rem',
                                        backgroundColor: '#1e293b',
                                        color: '#fff',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: 'pointer',
                                    }}
                                >
                                    Sensor Mode
                                </button>
                            </>
                        ) : (
                            <>
                                {loggedInUser ? (
                                    <>
                                        <span style={{ fontSize: '0.9rem' }}>
                                            Signed in as <strong>{loggedInUser.username}</strong>
                                        </span>
                                        <Link to="/users" style={{ fontSize: '0.85rem' }}>
                                            My Profile
                                        </Link>
                                        <button onClick={handleUserLogout} style={{ padding: '0.3rem 0.65rem', cursor: 'pointer' }}>
                                            Log out
                                        </button>
                                    </>
                                ) : (
                                    <>
                                        <span style={{ fontSize: '0.85rem', color: '#475569' }}>Browsing as Guest</span>
                                        <Link
                                            to="/login"
                                            style={{
                                                padding: '0.35rem 0.75rem',
                                                backgroundColor: '#2563eb',
                                                color: '#fff',
                                                textDecoration: 'none',
                                                borderRadius: '4px',
                                                fontSize: '0.85rem',
                                            }}
                                        >
                                            Sign in
                                        </Link>
                                    </>
                                )}

                                <button
                                    onClick={enableSensorMode}
                                    style={{
                                        padding: '0.35rem 0.7rem',
                                        fontSize: '0.8rem',
                                        backgroundColor: '#1e293b',
                                        color: '#fff',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: 'pointer',
                                    }}
                                >
                                    Sensor Mode
                                </button>
                            </>
                        )}
                    </div>
                </div>
            </header>

            {/* MAIN CONTENT */}
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
                        {mode === 'USER' && !isVendorSession && (
                            <UserPortal
                                selectedUserId={activeUserId}
                                vendorsWithEvents={vendorsWithEvents}
                                usersWithEvents={usersWithEvents}
                                onDataChanged={loadDirectoryData}
                            />
                        )}

                        {(mode === 'VENDOR' || isVendorSession) && (
                            <VendorPortal
                                selectedVendorId={activeVendorId}
                                onDataChanged={loadDirectoryData}
                            />
                        )}

                        {mode === 'ADMIN' && isSensorMode && (
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