import React from 'react';
import { EventUserResponseDto, EventVendorResponseDto } from '../types/event';

interface AdminDirectoryProps {
    vendorsWithEvents: EventVendorResponseDto[];
    usersWithEvents: EventUserResponseDto[];
}

export const AdminDirectory: React.FC<AdminDirectoryProps> = ({
                                                                  vendorsWithEvents,
                                                                  usersWithEvents,
                                                              }) => {
    return (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            {/* Vendors Column */}
            <section>
                <h2>Vendors & Hosted Events ({vendorsWithEvents.length})</h2>
                {vendorsWithEvents.length === 0 ? (
                    <p>No vendors found.</p>
                ) : (
                    vendorsWithEvents.map(({ vendors: vendor, events }) => (
                        <div
                            key={vendor.externalId}
                            style={{ border: '1px solid #ccc', padding: '1rem', marginBottom: '1rem' }}
                        >
                            <h3>{vendor.companyName}</h3>
                            <p style={{ fontSize: '0.9rem', color: '#555' }}>
                                {vendor.email} | {vendor.website}
                            </p>
                            <p style={{ fontSize: '0.8rem', color: '#777' }}>UUID: {vendor.externalId}</p>

                            <h4>Events ({events.length}):</h4>
                            {events.length === 0 ? (
                                <p style={{ fontStyle: 'italic', fontSize: '0.9rem' }}>No events created.</p>
                            ) : (
                                <ul>
                                    {events.map((ev) => (
                                        <li key={ev.externalEventId}>
                                            <strong>{ev.eventName}</strong> ({ev.eventCategory}) — {ev.eventLocation} [Cap: {ev.eventAttendance}]
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>
                    ))
                )}
            </section>

            {/* Users Column */}
            <section>
                <h2>Users & Created Events ({usersWithEvents.length})</h2>
                {usersWithEvents.length === 0 ? (
                    <p>No users found.</p>
                ) : (
                    usersWithEvents.map(({ user, events }) => (
                        <div
                            key={user.externalUserId}
                            style={{ border: '1px solid #ccc', padding: '1rem', marginBottom: '1rem' }}
                        >
                            <h3>
                                {user.firstName} {user.lastName} (@{user.username})
                            </h3>
                            <p style={{ fontSize: '0.9rem', color: '#555' }}>{user.email}</p>
                            <p style={{ fontSize: '0.8rem', color: '#777' }}>UUID: {user.externalUserId}</p>

                            <h4>Events ({events.length}):</h4>
                            {events.length === 0 ? (
                                <p style={{ fontStyle: 'italic', fontSize: '0.9rem' }}>No events created.</p>
                            ) : (
                                <ul>
                                    {events.map((ev) => (
                                        <li key={ev.externalEventId}>
                                            <strong>{ev.eventName}</strong> ({ev.eventCategory}) — {ev.eventLocation} [Cap: {ev.eventAttendance}]
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>
                    ))
                )}
            </section>
        </div>
    );
};