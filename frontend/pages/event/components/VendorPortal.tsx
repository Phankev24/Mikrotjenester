import React, { useEffect, useState } from 'react';
import { eventApi } from '../api.eventApi';
import {
    EventCreateDto,
    EventResponseDto,
    EventUpdateDto,
    EventVendorOverviewDto,
} from '../types/event';
import {OrganizerNotifications} from "./OrganizerNotifications.tsx";

interface VendorPortalProps {
    selectedVendorId: string;
    onDataChanged: () => void;
}

const EVENT_CATEGORIES = ['CONFERENCE', 'WORKSHOP', 'SOCIAL', 'SPORTS', 'MUSIC', 'OTHER'];

export const VendorPortal: React.FC<VendorPortalProps> = ({ selectedVendorId, onDataChanged }) => {
    const [overview, setOverview] = useState<EventVendorOverviewDto | null>(null);
    const [allEventsMap, setAllEventsMap] = useState<Record<string, EventResponseDto>>({});

    // Create Vendor Event Form
    const [createForm, setCreateForm] = useState<EventCreateDto>({
        eventName: '',
        eventDescription: '',
        eventCategory: EVENT_CATEGORIES[0],
        eventLocation: '',
        eventAttendance: 100,
    });

    // Inline Edit State
    const [editingEventId, setEditingEventId] = useState<string | null>(null);
    const [editForm, setEditForm] = useState<EventUpdateDto>({});

    const [statusMsg, setStatusMsg] = useState<string | null>(null);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    const loadVendorWorkspace = async () => {
        if (!selectedVendorId) return;
        setErrorMsg(null);
        try {
            const [vendorOverview, allEvents] = await Promise.all([
                eventApi.getVendorOrganizedEvents(selectedVendorId),
                eventApi.getAllEvents(),
            ]);

            setOverview(vendorOverview);

            // Map full EventResponseDto by UUID so editing has access to description & location
            const map: Record<string, EventResponseDto> = {};
            allEvents.forEach((ev) => {
                map[ev.externalEventId] = ev;
            });
            setAllEventsMap(map);
        } catch (err: any) {
            console.error(err);
            setErrorMsg(err.message || 'Failed to load vendor workspace.');
        }
    };

    useEffect(() => {
        setEditingEventId(null);
        setStatusMsg(null);
        loadVendorWorkspace();
    }, [selectedVendorId]);

    const handleCreateVendorEvent = async (e: React.FormEvent) => {
        e.preventDefault();
        setStatusMsg(null);
        setErrorMsg(null);
        try {
            await eventApi.createVendorEvent(selectedVendorId, createForm);
            setStatusMsg(`Published vendor event "${createForm.eventName}"!`);
            setCreateForm({
                eventName: '',
                eventDescription: '',
                eventCategory: EVENT_CATEGORIES[0],
                eventLocation: '',
                eventAttendance: 100,
            });
            await loadVendorWorkspace();
            onDataChanged();
        } catch (err: any) {
            setErrorMsg(err.message);
        }
    };

    const startEditing = (eventId: string) => {
        const fullEvent = allEventsMap[eventId];
        setEditingEventId(eventId);
        setEditForm({
            eventName: fullEvent?.eventName || '',
            eventDescription: fullEvent?.eventDescription || '',
            eventLocation: fullEvent?.eventLocation || '',
            eventCategory: fullEvent?.eventCategory || EVENT_CATEGORIES[0],
            eventAttendance: fullEvent?.eventAttendance || 50,
        });
    };

    const handleSaveEdit = async (eventId: string) => {
        setStatusMsg(null);
        setErrorMsg(null);
        try {
            await eventApi.updateEvent(eventId, editForm);
            setEditingEventId(null);
            setStatusMsg('Vendor event updated.');
            await loadVendorWorkspace();
            onDataChanged();
        } catch (err: any) {
            setErrorMsg(err.message);
        }
    };

    if (!selectedVendorId) {
        return <p>Select a vendor from the top bar to begin.</p>;
    }

    return (
        <div>
            {statusMsg && (
                <div style={{ padding: '0.75rem', backgroundColor: '#e6ffed', border: '1px solid green', marginBottom: '1rem' }}>
                    {statusMsg}
                </div>
            )}
            {errorMsg && (
                <div style={{ padding: '0.75rem', backgroundColor: '#ffe6e6', border: '1px solid red', marginBottom: '1rem' }}>
                    {errorMsg}
                </div>
            )}

            {/* Top Grid: Company Profile + Publish Vendor Event Form */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
                {/* Company Info Card */}
                <section style={{ border: '1px solid #ccc', padding: '1rem', backgroundColor: '#fff' }}>
                    <h3>Company Profile</h3>
                    {overview ? (
                        <div>
                            <h2 style={{ margin: '0.25rem 0' }}>{overview.vendor.companyName}</h2>
                            <p style={{ color: '#444' }}>{overview.vendor.description}</p>
                            <p style={{ fontSize: '0.9rem', margin: '0.25rem 0' }}>
                                <strong>Email:</strong> {overview.vendor.email}
                            </p>
                            <p style={{ fontSize: '0.9rem', margin: '0.25rem 0' }}>
                                <strong>Phone:</strong> {overview.vendor.phone}
                            </p>
                            <p style={{ fontSize: '0.9rem', margin: '0.25rem 0' }}>
                                <strong>Website:</strong> {overview.vendor.website}
                            </p>
                            <p style={{ fontSize: '0.8rem', color: '#777', marginTop: '0.5rem' }}>
                                Vendor UUID: <code>{overview.vendor.externalId}</code>
                            </p>
                        </div>
                    ) : (
                        <p>Loading company profile...</p>
                    )}
                    <OrganizerNotifications organizerId={selectedVendorId} type="vendor" />
                </section>

                {/* Create Vendor Event Form */}
                <section style={{ border: '1px solid #ccc', padding: '1rem', backgroundColor: '#fff' }}>
                    <h3>Publish Official Vendor Event</h3>
                    <form onSubmit={handleCreateVendorEvent} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                        <input
                            type="text"
                            placeholder="Event Name (e.g., Company Julebord, Half Marathon)"
                            required
                            value={createForm.eventName}
                            onChange={(e) => setCreateForm({ ...createForm, eventName: e.target.value })}
                        />
                        <textarea
                            placeholder="Event Description"
                            required
                            value={createForm.eventDescription}
                            onChange={(e) => setCreateForm({ ...createForm, eventDescription: e.target.value })}
                        />
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <select
                                value={createForm.eventCategory}
                                onChange={(e) => setCreateForm({ ...createForm, eventCategory: e.target.value })}
                            >
                                {EVENT_CATEGORIES.map((cat) => (
                                    <option key={cat} value={cat}>
                                        {cat}
                                    </option>
                                ))}
                            </select>
                            <input
                                type="text"
                                placeholder="Venue / Location"
                                required
                                style={{ flex: 1 }}
                                value={createForm.eventLocation}
                                onChange={(e) => setCreateForm({ ...createForm, eventLocation: e.target.value })}
                            />
                            <input
                                type="number"
                                min={1}
                                placeholder="Capacity"
                                required
                                style={{ width: '110px' }}
                                value={createForm.eventAttendance}
                                onChange={(e) => setCreateForm({ ...createForm, eventAttendance: Number(e.target.value) })}
                            />
                        </div>
                        <button type="submit">Publish Vendor Event</button>
                    </form>
                </section>
            </div>

            {/* Hosted Vendor Events & Attendee Rosters */}
            <section>
                <h2>Hosted Events & Registered Attendees ({overview?.hostedEvents.length || 0})</h2>
                {!overview || overview.hostedEvents.length === 0 ? (
                    <p>This vendor has not published any events yet.</p>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                        {overview.hostedEvents.map((ev) => {
                            const fullDetails = allEventsMap[ev.externalEventId];

                            return (
                                <div
                                    key={ev.externalEventId}
                                    style={{ border: '1px solid #bbb', padding: '1rem', backgroundColor: '#fff' }}
                                >
                                    {editingEventId === ev.externalEventId ? (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                            <h4>Edit Vendor Event</h4>
                                            <input
                                                value={editForm.eventName || ''}
                                                onChange={(e) => setEditForm({ ...editForm, eventName: e.target.value })}
                                            />
                                            <input
                                                value={editForm.eventDescription || ''}
                                                onChange={(e) => setEditForm({ ...editForm, eventDescription: e.target.value })}
                                            />
                                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                <input
                                                    value={editForm.eventLocation || ''}
                                                    onChange={(e) => setEditForm({ ...editForm, eventLocation: e.target.value })}
                                                />
                                                <input
                                                    type="number"
                                                    value={editForm.eventAttendance || 0}
                                                    onChange={(e) => setEditForm({ ...editForm, eventAttendance: Number(e.target.value) })}
                                                />
                                            </div>
                                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                <button onClick={() => handleSaveEdit(ev.externalEventId)}>Save Changes</button>
                                                <button onClick={() => setEditingEventId(null)}>Cancel</button>
                                            </div>
                                        </div>
                                    ) : (
                                        <>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                                <div>
                                                    <h3 style={{ margin: '0 0 0.25rem 0' }}>{ev.eventName}</h3>
                                                    {fullDetails && (
                                                        <>
                                                            <p style={{ margin: '0.25rem 0' }}>{fullDetails.eventDescription}</p>
                                                            <p style={{ fontSize: '0.9rem', color: '#444' }}>
                                                                Category: <strong>{fullDetails.eventCategory}</strong> | Location:{' '}
                                                                <strong>{fullDetails.eventLocation}</strong>
                                                            </p>
                                                        </>
                                                    )}
                                                    <p style={{ fontSize: '0.95rem', marginTop: '0.5rem' }}>
                                                        <strong>Attendance:</strong> {ev.currentAttendanceCount} / {ev.maxCapacity} registered (
                                                        {ev.remainingCapacity} spots left)
                                                    </p>
                                                </div>

                                                <button onClick={() => startEditing(ev.externalEventId)}>Edit Event</button>
                                            </div>

                                            {/* Attendee Roster Table */}
                                            <div style={{ marginTop: '0.75rem', paddingTop: '0.75rem', borderTop: '1px solid #eee' }}>
                                                <h4 style={{ margin: '0 0 0.5rem 0' }}>Attendee Roster:</h4>
                                                {ev.attendees.length === 0 ? (
                                                    <p style={{ fontStyle: 'italic', color: '#666', margin: 0 }}>No attendees registered yet.</p>
                                                ) : (
                                                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
                                                        <thead>
                                                        <tr style={{ textAlign: 'left', borderBottom: '1px solid #ccc' }}>
                                                            <th style={{ padding: '0.35rem' }}>Name</th>
                                                            <th style={{ padding: '0.35rem' }}>Username</th>
                                                            <th style={{ padding: '0.35rem' }}>Email</th>
                                                            <th style={{ padding: '0.35rem' }}>Phone</th>
                                                        </tr>
                                                        </thead>
                                                        <tbody>
                                                        {ev.attendees.map((att) => (
                                                            <tr key={att.externalUserId} style={{ borderBottom: '1px solid #eee' }}>
                                                                <td style={{ padding: '0.35rem' }}>
                                                                    {att.firstName} {att.lastName}
                                                                </td>
                                                                <td style={{ padding: '0.35rem' }}>@{att.username}</td>
                                                                <td style={{ padding: '0.35rem' }}>{att.email}</td>
                                                                <td style={{ padding: '0.35rem' }}>{att.phoneNumber}</td>
                                                            </tr>
                                                        ))}
                                                        </tbody>
                                                    </table>
                                                )}
                                            </div>
                                        </>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                )}
            </section>
        </div>
    );
};