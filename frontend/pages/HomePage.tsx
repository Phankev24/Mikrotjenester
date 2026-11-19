import React, { useState } from 'react';
import { Link } from 'react-router-dom';

type TabKey = 'events' | 'user' | 'vendor';

export default function HomePage() {
    const [activeTab, setActiveTab] = useState<TabKey>('events');

    const tabs: { key: TabKey; label: string; to: string; title: string; subtitle: string; cta: string }[] = [
        {
            key: 'events',
            label: 'View Events',
            to: '/events',
            title: 'Browse All Events',
            subtitle: 'Explore upcoming community and official vendor events across the platform.',
            cta: 'Go to Events',
        },
        {
            key: 'user',
            label: 'User Login',
            to: '/users',
            title: 'User Portal',
            subtitle: 'Sign in as a user to join events, view your schedule, or host community events.',
            cta: 'Continue as User',
        },
        {
            key: 'vendor',
            label: 'Vendor Login',
            to: '/vendor',
            title: 'Vendor Portal',
            subtitle: 'Sign in as a vendor to publish official events and track attendee notifications.',
            cta: 'Continue as Vendor',
        },
    ];

    const current = tabs.find((t) => t.key === activeTab)!;

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
            <h1 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '0.4rem' }}>
                Event Planning &amp; Management
            </h1>
            <p style={{ color: '#64748b', marginTop: 0, marginBottom: '1.75rem' }}>
                Browse public events or sign in to your user or vendor workspace.
            </p>

            {/* Top Navigation Tabs */}
            <div
                style={{
                    display: 'flex',
                    borderBottom: '2px solid #e2e8f0',
                    gap: '0.5rem',
                    marginBottom: '1.75rem',
                }}
            >
                {tabs.map((tab) => {
                    const isActive = activeTab === tab.key;
                    return (
                        <button
                            key={tab.key}
                            onClick={() => setActiveTab(tab.key)}
                            style={{
                                padding: '0.75rem 1.25rem',
                                fontSize: '0.95rem',
                                fontWeight: isActive ? 600 : 500,
                                color: isActive ? '#2563eb' : '#475569',
                                backgroundColor: 'transparent',
                                border: 'none',
                                borderBottom: isActive ? '2px solid #2563eb' : '2px solid transparent',
                                marginBottom: '-2px',
                                cursor: 'pointer',
                            }}
                        >
                            {tab.label}
                        </button>
                    );
                })}
            </div>

            {/* Active Tab Panel */}
            <div
                style={{
                    padding: '2rem',
                    border: '1px solid #e2e8f0',
                    borderRadius: '10px',
                    backgroundColor: '#ffffff',
                    boxShadow: '0 1px 3px rgba(15, 23, 42, 0.05)',
                }}
            >
                <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.35rem' }}>{current.title}</h2>
                <p style={{ margin: '0 0 1.5rem 0', color: '#475569', lineHeight: 1.5 }}>
                    {current.subtitle}
                </p>

                <Link
                    to={current.to}
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
                    {current.cta} &rarr;
                </Link>
            </div>
        </div>
    );
}