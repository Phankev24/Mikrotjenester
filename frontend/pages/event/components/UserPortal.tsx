import React, { useEffect, useState } from 'react';
import { eventApi } from '../api.eventApi';
import {
    EventAttendanceResponseDto,
    EventCreateDto,
    EventResponseDto,
    EventUpdateDto,
} from '../types/event';

interface UserPortalProps {
    selectedUserId: string;
    onDataChanged: () => void; // Refreshes parent directory state after creating/updating
}

// Adjust these strings to match your Java EventCategory enum constants
const EVENT_CATEGORIES = ['SOCIAL', 'SPORTS', 'MUSIC', 'WORKSHOP', 'CONFERENCE', 'OTHER'];

export const UserPortal: React.FC<UserPortalProps> = ({ selectedUserId, onDataChanged }) => {
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
        if (!selectedUserId) return;
        setErrorMsg(null);
        try {
            const [eventsData, attendingData, organizedData] = await Promise.all([
                eventApi.getAllEvents(),
                eventApi.getUserAttendingEvents(selectedUserId),
                eventApi.getUserOrganizedEvents(selectedUserId),
            ]);
            setAllEvents(eventsData);
            setJoinedEvents(attendingData.joinedEvents);
            setHostedEvents(organizedData.hostedEvents);
        } catch (err: any) {
            console.error(err);
            setErrorMsg(err.message || 'Failed to load user workspace data.');
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

    if (!selectedUserId) {
        return <p>Select a user from the top bar to begin.</p>;
    }

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

            {/* Top Grid: Create Event + My Personal Activity */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
                {/* Create User Event Form */}
                <section style={{ border: '1px solid #ccc', padding: '1rem' }}>
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
                <section style={{ border: '1px solid #ccc', padding: '1rem' }}>
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

            {/* Global Public Event Feed */}
            <section>
                <h2>All Available Events ({allEvents.length})</h2>
                {allEvents.length === 0 ? (
                    <p>No events found in the database.</p>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {allEvents.map((event) => {
                            const isJoined = joinedEventIds.has(event.externalEventId);
                            const isOrganizer = event.eventOrganizer === selectedUserId;
                            const isVendorEvent = Boolean(event.externalVendorId);

                            return (
                                <div key={event.externalEventId} style={{ border: '1px solid #bbb', padding: '1rem' }}>
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
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                                <div>
                          <span
                              style={{
                                  fontSize: '0.75rem',
                                  padding: '0.15rem 0.4rem',
                                  backgroundColor: isVendorEvent ? '#dbeafe' : '#fef3c7',
                                  border: '1px solid #999',
                                  marginRight: '0.5rem',
                              }}
                          >
                            {isVendorEvent ? 'VENDOR EVENT' : 'COMMUNITY EVENT'}
                          </span>
                                                    <span style={{ fontSize: '0.8rem', color: '#555' }}>[{event.eventCategory}]</span>
                                                    <h3 style={{ margin: '0.4rem 0' }}>{event.eventName}</h3>
                                                    <p style={{ margin: '0.25rem 0' }}>{event.eventDescription}</p>
                                                    <p style={{ fontSize: '0.9rem', color: '#444' }}>
                                                        Location: <strong>{event.eventLocation}</strong> | Max Capacity:{' '}
                                                        <strong>{event.eventAttendance}</strong>
                                                        {event.eventDateTime && ` | Date: ${new Date(event.eventDateTime).toLocaleString()}`}
                                                    </p>
                                                </div>

                                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                    {isJoined ? (
                                                        <button onClick={() => handleLeaveEvent(event.externalEventId)}>Leave Event</button>
                                                    ) : (
                                                        <button onClick={() => handleJoinEvent(event.externalEventId)}>Join Event</button>
                                                    )}

                                                    <button onClick={() => handleToggleAttendees(event.externalEventId)}>
                                                        {expandedEventId === event.externalEventId ? 'Hide Attendees' : 'View Attendees'}
                                                    </button>

                                                    {isOrganizer && (
                                                        <button onClick={() => startEditing(event)}>Edit</button>
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