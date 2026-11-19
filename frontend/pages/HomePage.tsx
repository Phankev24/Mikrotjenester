import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

type TabKey = 'events' | 'user' | 'vendor';

export default function HomePage() {
    const [activeTab, setActiveTab] = useState<TabKey>('events');
    const navigate = useNavigate();

    const openSensorMode = () => {
        localStorage.setItem('epm_role', 'SENSOR');
        navigate('/events');
    };

    const openNormalUserView = () => {
        localStorage.setItem('epm_role', 'USER');
        navigate('/events');
    };

    return (
        <div
            style={{
                maxWidth: '760px',
                margin: '4rem auto',
                padding: '0 1.5rem',
                fontFamily: 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
                color: '#0f172a',
            }}
        >
            {/* Topptekst + Sensor-knapp */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                <h1 style={{ fontSize: '2rem', fontWeight: 700, margin: 0 }}>
                    Event Planning &amp; Management
                </h1>

                <button
                    onClick={openSensorMode}
                    style={{
                        padding: '0.45rem 0.9rem',
                        fontSize: '0.82rem',
                        fontWeight: 600,
                        backgroundColor: '#1e293b',
                        color: '#ffffff',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                    }}
                >
                    Sensor / Admin Mode
                </button>
            </div>

            <p style={{ color: '#64748b', marginTop: 0, marginBottom: '1.75rem' }}>
                Se arrangementer eller gå videre til innlogging for bruker eller vendor.
            </p>

            {/* Faner */}
            <div
                style={{
                    display: 'flex',
                    borderBottom: '2px solid #e2e8f0',
                    gap: '0.5rem',
                    marginBottom: '1.75rem',
                }}
            >
                <button
                    onClick={() => setActiveTab('events')}
                    style={{
                        padding: '0.75rem 1.25rem',
                        fontSize: '0.95rem',
                        fontWeight: activeTab === 'events' ? 600 : 500,
                        color: activeTab === 'events' ? '#2563eb' : '#475569',
                        backgroundColor: 'transparent',
                        border: 'none',
                        borderBottom: activeTab === 'events' ? '2px solid #2563eb' : '2px solid transparent',
                        marginBottom: '-2px',
                        cursor: 'pointer',
                    }}
                >
                    View Events
                </button>

                <button
                    onClick={() => setActiveTab('user')}
                    style={{
                        padding: '0.75rem 1.25rem',
                        fontSize: '0.95rem',
                        fontWeight: activeTab === 'user' ? 600 : 500,
                        color: activeTab === 'user' ? '#2563eb' : '#475569',
                        backgroundColor: 'transparent',
                        border: 'none',
                        borderBottom: activeTab === 'user' ? '2px solid #2563eb' : '2px solid transparent',
                        marginBottom: '-2px',
                        cursor: 'pointer',
                    }}
                >
                    User Login
                </button>

                <button
                    onClick={() => setActiveTab('vendor')}
                    style={{
                        padding: '0.75rem 1.25rem',
                        fontSize: '0.95rem',
                        fontWeight: activeTab === 'vendor' ? 600 : 500,
                        color: activeTab === 'vendor' ? '#059669' : '#475569',
                        backgroundColor: 'transparent',
                        border: 'none',
                        borderBottom: activeTab === 'vendor' ? '2px solid #059669' : '2px solid transparent',
                        marginBottom: '-2px',
                        cursor: 'pointer',
                    }}
                >
                    Vendor Login
                </button>
            </div>

            {/* Aktivt panel */}
            <div
                style={{
                    padding: '2rem',
                    border: '1px solid #e2e8f0',
                    borderRadius: '10px',
                    backgroundColor: '#ffffff',
                    boxShadow: '0 1px 3px rgba(15, 23, 42, 0.05)',
                }}
            >
                {activeTab === 'events' && (
                    <>
                        <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.35rem' }}>User Portal &amp; Events</h2>
                        <p style={{ margin: '0 0 1.5rem 0', color: '#475569', lineHeight: 1.5 }}>
                            Utforsk tilgjengelige eventer, meld deg på arrangementer eller opprett community-eventer.
                        </p>
                        <button
                            onClick={openNormalUserView}
                            style={{
                                padding: '0.65rem 1.35rem',
                                backgroundColor: '#2563eb',
                                color: '#ffffff',
                                border: 'none',
                                borderRadius: '8px',
                                fontWeight: 600,
                                fontSize: '0.95rem',
                                cursor: 'pointer',
                            }}
                        >
                            Åpne User Portal &rarr;
                        </button>
                    </>
                )}

                {activeTab === 'user' && (
                    <>
                        <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.35rem' }}>User Login</h2>
                        <p style={{ margin: '0 0 1.5rem 0', color: '#475569', lineHeight: 1.5 }}>
                            Gå til brukerinnlogging for å administrere din profil og dine påmeldinger.
                        </p>
                        <Link
                            to="/users"
                            onClick={() => localStorage.setItem('epm_role', 'USER')}
                            style={{
                                display: 'inline-block',
                                padding: '0.65rem 1.35rem',
                                backgroundColor: '#2563eb',
                                color: '#ffffff',
                                textDecoration: 'none',
                                borderRadius: '8px',
                                fontWeight: 600,
                                fontSize: '0.95rem',
                            }}
                        >
                            Gå til User Login &rarr;
                        </Link>
                    </>
                )}

                {activeTab === 'vendor' && (
                    <>
                        <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.35rem' }}>Vendor Login</h2>
                        <p style={{ margin: '0 0 1.5rem 0', color: '#475569', lineHeight: 1.5 }}>
                            Gå til vendor-innlogging for å administrere bedriftsprofil og offisielle eventer.
                        </p>
                        <Link
                            to="/vendor"
                            onClick={() => localStorage.setItem('epm_role', 'VENDOR')}
                            style={{
                                display: 'inline-block',
                                padding: '0.65rem 1.35rem',
                                backgroundColor: '#059669',
                                color: '#ffffff',
                                textDecoration: 'none',
                                borderRadius: '8px',
                                fontWeight: 600,
                                fontSize: '0.95rem',
                            }}
                        >
                            Gå til Vendor Login &rarr;
                        </Link>
                    </>
                )}
            </div>
        </div>
    );
}