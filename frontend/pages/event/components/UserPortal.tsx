import React, { useEffect, useState } from 'react';
import { eventApi } from '../api.eventApi';
import {
    EventAttendanceResponseDto,
    EventCreateDto,
    EventResponseDto,
    EventUpdateDto,
    EventUserResponseDto,
    EventVendorResponseDto,
} from '../types/event';
import { OrganizerNotifications } from './OrganizerNotifications.tsx';

interface UserPortalProps {
    selectedUserId: string;
    vendorsWithEvents: EventVendorResponseDto[];
    usersWithEvents: EventUserResponseDto[];
    onDataChanged: () => void;
}

const EVENT_CATEGORIES = ['SOCIAL', 'SPORTS', 'MUSIC', 'CONFERENCE', 'DINNER', 'PERFORMANCE'];

export const UserPortal: React.FC<UserPortalProps> = ({
                                                          selectedUserId,
                                                          vendorsWithEvents,
                                                          usersWithEvents,
                                                          onDataChanged,
                                                      }) => {
    const [allEvents, setAllEvents] = useState<EventResponseDto[]>([]);
    const [joinedEvents, setJoinedEvents] = useState<EventAttendanceResponseDto[]>([]);
    const [hostedEvents, setHostedEvents] = useState<EventAttendanceResponseDto[]>([]);

    // Track which event has its attendee list expanded
    const [expandedEventId, setExpandedEventId] = useState<string | null>(null);
    const [expandedAttendance, setExpandedAttendance] = useState<EventAttendanceResponseDto | null>(null);

    // Create form state
    const [createForm, setCreateForm] = useState<EventCreateDto>({
        eventName: '',
        eventDescription: '',
        eventCategory: EVENT_CATEGORIES[0],
        eventLocation: '',
        eventAttendance: 20,
    });

    // Edit state
    const [editingEventId, setEditingEventId] = useState<string | null>(null);
    const [editForm, setEditForm] = useState<EventUpdateDto>({});

    const [statusMsg, setStatusMsg] = useState<string | null>(null);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    const loadUserWorkspace = async () => {
        setErrorMsg(null);
        try {
            const eventsData = await eventApi.getAllEvents();
            setAllEvents(eventsData);

            if (selectedUserId) {
                const [attendingData, organizedData] = await Promise.all([
                    eventApi.getUserAttendingEvents(selectedUserId),
                    eventApi.getUserOrganizedEvents(selectedUserId),
                ]);
                setJoinedEvents(attendingData.joinedEvents);
                setHostedEvents(organizedData.hostedEvents);
            } else {
                setJoinedEvents([]);
                setHostedEvents([]);
            }
        } catch (err: any) {
            console.error(err);
            setErrorMsg(err.message || 'Failed to load workspace data.');
        }
    };

    useEffect(() => {
        setExpandedEventId(null);
        setExpandedAttendance(null);
        setEditingEventId(null);
        loadUserWorkspace();
    }, [selectedUserId]);

    const handleToggleAttendees = async (eventId: string) => {
        if (expandedEventId === eventId) {
            setExpandedEventId(null);
            setExpandedAttendance(null);
            return;
        }
        try {
            const data = await eventApi.getEventAttendees(eventId);
            setExpandedEventId(eventId);
            setExpandedAttendance(data);
        } catch (err: any) {
            setErrorMsg(err.message);
        }
    };

    const handleJoinEvent = async (eventId: string) => {
        if (!selectedUserId) return;
        setStatusMsg(null);
        setErrorMsg(null);
        try {
            await eventApi.registerAttendee(eventId, selectedUserId);
            setStatusMsg('Successfully registered for event!');
            await loadUserWorkspace();
            if (expandedEventId === eventId) {
                const updated = await eventApi.getEventAttendees(eventId);
                setExpandedAttendance(updated);
            }
        } catch (err: any) {
            setErrorMsg(err.message || 'Could not register (may be full or already joined).');
        }
    };

    const handleLeaveEvent = async (eventId: string) => {
        if (!selectedUserId) return;
        setStatusMsg(null);
        setErrorMsg(null);
        try {
            await eventApi.removeAttendee(eventId, selectedUserId);
            setStatusMsg('Removed registration from event.');
            await loadUserWorkspace();
            if (expandedEventId === eventId) {
                const updated = await eventApi.getEventAttendees(eventId);
                setExpandedAttendance(updated);
            }
        } catch (err: any) {
            setErrorMsg(err.message);
        }
    };

    const handleCreateEvent = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedUserId) return;
        setStatusMsg(null);
        setErrorMsg(null);
        try {
            await eventApi.createUserEvent(selectedUserId, createForm);
            setStatusMsg(`Created event "${createForm.eventName}"!`);
            setCreateForm({
                eventName: '',
                eventDescription: '',
                eventCategory: EVENT_CATEGORIES[0],
                eventLocation: '',
                eventAttendance: 20,
            });
            await loadUserWorkspace();
            onDataChanged();
        } catch (err: any) {
            setErrorMsg(err.message);
        }
    };

    const startEditing = (event: EventResponseDto) => {
        setEditingEventId(event.externalEventId);
        setEditForm({
            eventName: event.eventName,
            eventDescription: event.eventDescription,
            eventLocation: event.eventLocation,
            eventCategory: event.eventCategory,
            eventAttendance: event.eventAttendance,
        });
    };

    const handleSaveEdit = async (eventId: string) => {
        setStatusMsg(null);
        setErrorMsg(null);
        try {
            await eventApi.updateEvent(eventId, editForm);
            setEditingEventId(null);
            setStatusMsg('Event updated.');
            await loadUserWorkspace();
            onDataChanged();
        } catch (err: any) {
            setErrorMsg(err.message);
        }
    };

    const handleDeleteEvent = async (eventId: string, eventName: string) => {
        if (!window.confirm(`Delete event "${eventName}"?`)) return;
        setStatusMsg(null);
        setErrorMsg(null);
        try {
            await eventApi.deleteEvent(eventId);
            if (expandedEventId === eventId) {
                setExpandedEventId(null);
                setExpandedAttendance(null);
            }
            setStatusMsg(`Deleted event "${eventName}".`);
            await loadUserWorkspace();
            onDataChanged();
        } catch (err: any) {
            setErrorMsg(err.message);
        }
    };

    const resolveOrganizerInfo = (event: EventResponseDto) => {
        if (event.externalVendorId) {
            const match = vendorsWithEvents.find(
                ({ vendors: v }) => v.externalId === event.externalVendorId
            );
            const v = match?.vendors;
            return {
                roleLabel: 'Official Vendor',
                name: v ? v.companyName : 'Verified Vendor',
                email: v?.email,
                extra: v?.website || v?.phone,
                id: event.externalVendorId,
            };
        }

        const organizerUuid = event.externalUserId || event.eventOrganizer;
        const match = usersWithEvents.find(
            ({ user: u }) => u.externalUserId === organizerUuid
        );
        const u = match?.user;
        return {
            roleLabel: 'Community Organizer',
            name: u ? `${u.firstName} ${u.lastName} (@${u.username})` : 'Community Member',
            email: u?.email,
            extra: u?.phoneNumber,
            id: organizerUuid,
        };
    };

    const joinedEventIds = new Set(joinedEvents.map((e) => e.externalEventId));

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

            {/* Top Grid: Create Event + My Personal Activity (only shown when signed in) */}
            {selectedUserId ? (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
                    {/* Create User Event Form */}
                    <section style={{ border: '1px solid #ccc', padding: '1rem', backgroundColor: '#fff' }}>
                        <h3>Host a Community Event</h3>
                        <form onSubmit={handleCreateEvent} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                            <input
                                type="text"
                                placeholder="Event Name (e.g., Woods Rave, Birthday Dinner)"
                                required
                                value={createForm.eventName}
                                onChange={(e) => setCreateForm({ ...createForm, eventName: e.target.value })}
                            />
                            <textarea
                                placeholder="Description"
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
                                    placeholder="Location"
                                    required
                                    style={{ flex: 1 }}
                                    value={createForm.eventLocation}
                                    onChange={(e) => setCreateForm({ ...createForm, eventLocation: e.target.value })}
                                />
                                <input
                                    type="number"
                                    min={1}
                                    placeholder="Max Capacity"
                                    required
                                    style={{ width: '110px' }}
                                    value={createForm.eventAttendance}
                                    onChange={(e) => setCreateForm({ ...createForm, eventAttendance: Number(e.target.value) })}
                                />
                            </div>
                            <button type="submit">Create User Event</button>
                        </form>
                    </section>

                    {/* My Joined & Hosted Events Summary */}
                    <section style={{ border: '1px solid #ccc', padding: '1rem', backgroundColor: '#fff' }}>
                        <h3>My Activity</h3>

                        <h4>Events I'm Attending ({joinedEvents.length})</h4>
                        {joinedEvents.length === 0 ? (
                            <p style={{ fontSize: '0.9rem', color: '#666' }}>Not registered for any events yet.</p>
                        ) : (
                            <ul>
                                {joinedEvents.map((ev) => (
                                    <li key={ev.externalEventId} style={{ marginBottom: '0.4rem' }}>
                                        <strong>{ev.eventName}</strong> ({ev.currentAttendanceCount}/{ev.maxCapacity} spots){' '}
                                        <button onClick={() => handleLeaveEvent(ev.externalEventId)}>Leave</button>
                                    </li>
                                ))}
                            </ul>
                        )}
                        <div style={{ marginTop: '1rem' }}>
                            <OrganizerNotifications organizerId={selectedUserId} type="user" />
                        </div>

                        <h4>Events I Created ({hostedEvents.length})</h4>
                        {hostedEvents.length === 0 ? (
                            <p style={{ fontSize: '0.9rem', color: '#666' }}>You haven't hosted any events.</p>
                        ) : (
                            <ul>
                                {hostedEvents.map((ev) => (
                                    <li key={ev.externalEventId} style={{ marginBottom: '0.4rem' }}>
                                        <strong>{ev.eventName}</strong> — {ev.currentAttendanceCount}/{ev.maxCapacity} attending
                                    </li>
                                ))}
                            </ul>
                        )}
                    </section>
                </div>
            ) : (
                <div
                    style={{
                        padding: '1rem 1.25rem',
                        backgroundColor: '#eff6ff',
                        border: '1px solid #bfdbfe',
                        borderRadius: '6px',
                        marginBottom: '1.5rem',
                        color: '#1e3a8a',
                    }}
                >
                    You are browsing as a guest. <strong>Sign in</strong> to join events or create your own community events.
                </div>
            )}

            {/* Global Public Event Feed */}
            <section>
                <h2>All Available Events ({allEvents.length})</h2>
                {allEvents.length === 0 ? (
                    <p>No events found in the database.</p>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {allEvents.map((event) => {
                            const isJoined = joinedEventIds.has(event.externalEventId);
                            const isOrganizer = Boolean(
                                selectedUserId &&
                                (event.eventOrganizer === selectedUserId ||
                                    event.externalUserId === selectedUserId)
                            );
                            const isVendorEvent = Boolean(event.externalVendorId);
                            const organizer = resolveOrganizerInfo(event);

                            return (
                                <div
                                    key={event.externalEventId}
                                    style={{
                                        border: '1px solid #bbb',
                                        borderLeft: `4px solid ${isVendorEvent ? '#2563eb' : '#d97706'}`,
                                        padding: '1rem',
                                        backgroundColor: '#fff',
                                    }}
                                >
                                    {editingEventId === event.externalEventId ? (
                                        /* Inline Edit Form */
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                            <h4>Editing Event</h4>
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
                                                <button onClick={() => handleSaveEdit(event.externalEventId)}>Save Changes</button>
                                                <button onClick={() => setEditingEventId(null)}>Cancel</button>
                                            </div>
                                        </div>
                                    ) : (
                                        /* Standard Event Card */
                                        <>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem' }}>
                                                <div style={{ flex: 1 }}>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                                                        <span
                                                            style={{
                                                                fontSize: '0.75rem',
                                                                fontWeight: 600,
                                                                padding: '0.15rem 0.45rem',
                                                                backgroundColor: isVendorEvent ? '#dbeafe' : '#fef3c7',
                                                                border: '1px solid #999',
                                                            }}
                                                        >
                                                            {isVendorEvent ? 'VENDOR EVENT' : 'COMMUNITY EVENT'}
                                                        </span>
                                                        <span style={{ fontSize: '0.8rem', color: '#555' }}>[{event.eventCategory}]</span>
                                                        {isOrganizer && (
                                                            <span
                                                                style={{
                                                                    fontSize: '0.75rem',
                                                                    fontWeight: 600,
                                                                    padding: '0.15rem 0.45rem',
                                                                    backgroundColor: '#dcfce7',
                                                                    color: '#166534',
                                                                    border: '1px solid #86efac',
                                                                }}
                                                            >
                                                                Your Event
                                                            </span>
                                                        )}
                                                    </div>

                                                    <h3 style={{ margin: '0.45rem 0 0.25rem 0' }}>{event.eventName}</h3>
                                                    <p style={{ margin: '0.25rem 0 0.65rem 0' }}>{event.eventDescription}</p>

                                                    {/* Organizer Details Box */}
                                                    <div
                                                        style={{
                                                            padding: '0.5rem 0.75rem',
                                                            marginBottom: '0.6rem',
                                                            backgroundColor: '#f8fafc',
                                                            border: '1px solid #e2e8f0',
                                                            borderRadius: '4px',
                                                            fontSize: '0.85rem',
                                                            color: '#334155',
                                                        }}
                                                    >
                                                        <strong>Organizer ({organizer.roleLabel}):</strong> {organizer.name}
                                                        {organizer.email && <span> | Email: {organizer.email}</span>}
                                                        {organizer.extra && <span> | {organizer.extra}</span>}
                                                        <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.15rem' }}>
                                                            Organizer ID: <code>{organizer.id}</code>
                                                        </div>
                                                    </div>

                                                    <p style={{ fontSize: '0.9rem', color: '#444', margin: 0 }}>
                                                        Location: <strong>{event.eventLocation}</strong> | Max Capacity:{' '}
                                                        <strong>{event.eventAttendance}</strong>
                                                        {event.eventDateTime && ` | Date: ${new Date(event.eventDateTime).toLocaleString()}`}
                                                    </p>
                                                </div>

                                                <div style={{ display: 'flex', gap: '0.5rem', flexShrink: 0 }}>
                                                    {!selectedUserId ? (
                                                        <span
                                                            style={{
                                                                fontSize: '0.85rem',
                                                                color: '#64748b',
                                                                alignSelf: 'center',
                                                                padding: '0.35rem 0.65rem',
                                                                backgroundColor: '#f1f5f9',
                                                                borderRadius: '4px',
                                                            }}
                                                        >
                                                            Sign in to join
                                                        </span>
                                                    ) : isOrganizer ? (
                                                        <button
                                                            disabled
                                                            title="You cannot join an event you organized"
                                                            style={{
                                                                backgroundColor: '#f1f5f9',
                                                                color: '#64748b',
                                                                border: '1px solid #cbd5e1',
                                                                cursor: 'not-allowed',
                                                                padding: '0.35rem 0.65rem',
                                                            }}
                                                        >
                                                            Host (Cannot Join)
                                                        </button>
                                                    ) : isJoined ? (
                                                        <button onClick={() => handleLeaveEvent(event.externalEventId)}>Leave Event</button>
                                                    ) : (
                                                        <button onClick={() => handleJoinEvent(event.externalEventId)}>Join Event</button>
                                                    )}

                                                    <button onClick={() => handleToggleAttendees(event.externalEventId)}>
                                                        {expandedEventId === event.externalEventId ? 'Hide Attendees' : 'View Attendees'}
                                                    </button>

                                                    {isOrganizer && (
                                                        <>
                                                            <button onClick={() => startEditing(event)}>Edit</button>
                                                            <button
                                                                onClick={() => handleDeleteEvent(event.externalEventId, event.eventName)}
                                                                style={{ backgroundColor: '#fee2e2', borderColor: '#ef4444' }}
                                                            >
                                                                Delete
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            </div>

                                            {/* Expanded Attendee Drawer */}
                                            {expandedEventId === event.externalEventId && expandedAttendance && (
                                                <div style={{ marginTop: '1rem', padding: '0.75rem', backgroundColor: '#f9f9f9', borderTop: '1px solid #ddd' }}>
                                                    <p style={{ margin: '0 0 0.5rem 0' }}>
                                                        <strong>Capacity:</strong> {expandedAttendance.currentAttendanceCount} /{' '}
                                                        {expandedAttendance.maxCapacity} ({expandedAttendance.remainingCapacity} remaining)
                                                    </p>
                                                    {expandedAttendance.attendees.length === 0 ? (
                                                        <p style={{ fontStyle: 'italic', margin: 0 }}>No attendees registered yet.</p>
                                                    ) : (
                                                        <ul style={{ margin: 0 }}>
                                                            {expandedAttendance.attendees.map((att) => (
                                                                <li key={att.externalUserId}>
                                                                    {att.firstName} {att.lastName} ({att.email})
                                                                </li>
                                                            ))}
                                                        </ul>
                                                    )}
                                                </div>
                                            )}
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