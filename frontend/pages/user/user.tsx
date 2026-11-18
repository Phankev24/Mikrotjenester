import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { userApi } from './userApi';
import { useAuth } from '../auth/authContext';
import { EVENT_CATEGORIES, EventCategory, EventDto, UserResponseDto } from './types/user';

const GENDERS = ['Male', 'Female', 'Alien', 'Other'];

const boxStyle: React.CSSProperties = { border: '1px solid #ccc', padding: '1rem', marginBottom: '1.5rem'};
const formStyle: React.CSSProperties = { display: 'flex', flexDirection: 'column', gap: '0.75rem' };
const rowStyle: React.CSSProperties = { display: 'flex', gap: '0.5rem' };
const labelStyle: React.CSSProperties = { display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.9rem' };
const primaryBtn: React.CSSProperties = {
    padding: '0.4rem 0.8rem',
    backgroundColor: '#2563eb',
    border: '1px solid #999',
    cursor: 'pointer',
};
const tagStyle: React.CSSProperties = {
    fontSize: '0.75rem',
    padding: '0.15rem 0.4rem',
    backgroundColor: '#dbeafe',
    border: '1px solid #999',
    marginRight: '0.5rem',
};

const emptyUserForm = { username: '', password: '', firstName: '', lastName: '', email: '', phoneNumber: '', age: '', gender: '' };
const emptyEventForm = { eventName: '', eventDescription: '', eventCategory: 'SOCIAL' as EventCategory, eventLocation: '', eventAttendance: '0' };

export default function UserPage() {
    const navigate = useNavigate();
    const { user: loggedInUser, logout } = useAuth();
    const [users, setUsers] = useState<UserResponseDto[]>([]);
    const [selectedUserId, setSelectedUserId] = useState<number | null>(loggedInUser?.id ?? null);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };
    const [events, setEvents] = useState<EventDto[]>([]);

    const [userForm, setUserForm] = useState(emptyUserForm);
    const [eventForm, setEventForm] = useState(emptyEventForm);
    const [editing, setEditing] = useState<boolean>(false);

    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const selectedUser = users.find((u) => u.id === selectedUserId) ?? null;

    const loadUsers = async (keepSelection?: number | null) => {
        setLoading(true);
        setError(null);
        try {
            const data = await userApi.getUsers();
            setUsers(data);
            const target = keepSelection ?? selectedUserId;
            setSelectedUserId(data.some((u) => u.id === target) ? target : (data[0]?.id ?? null));
        } catch (err) {
            console.error(err);
            setError('Failed to load users. Ensure the User service (8081) is running.');
        } finally {
            setLoading(false);
        }
    };

    const loadEvents = async (id: number) => {
        try {
            const data = await userApi.getUserWithEvents(id);
            setEvents(data.events);
        } catch (err) {
            console.error(err);
            setEvents([]);
            setError('Failed to load events. Ensure the Event service (8080) is running.');
        }
    };

    useEffect(() => {
        loadUsers();
    }, []);

    useEffect(() => {
        setEditing(false);
        if (selectedUserId !== null) loadEvents(selectedUserId);
        else setEvents([]);
    }, [selectedUserId]);

    const run = async (action: () => Promise<void>) => {
        setError(null);
        try {
            await action();
        } catch (err) {
            console.error(err);
            setError(err instanceof Error ? err.message : 'Something went wrong.');
        }
    };

    const toUserDto = () => ({
        username: userForm.username,
        password: userForm.password,
        firstName: userForm.firstName || undefined,
        lastName: userForm.lastName || undefined,
        email: userForm.email || undefined,
        phoneNumber: userForm.phoneNumber || undefined,
        age: userForm.age ? Number(userForm.age) : undefined,
        gender: userForm.gender || undefined,
    });

    const handleSaveUser = (e: React.FormEvent) => {
        e.preventDefault();
        run(async () => {
            if (editing && selectedUser) {
                const { password, ...rest } = toUserDto();
                await userApi.updateUser(selectedUser.id, password ? { ...rest, password } : rest);
                setEditing(false);
                await loadUsers(selectedUser.id);
            } else {
                const created = await userApi.createUser(toUserDto());
                await loadUsers(created.id);
            }
            setUserForm(emptyUserForm);
        });
    };

    const startEdit = () => {
        if (!selectedUser) return;
        setUserForm({
            username: selectedUser.username,
            password: '',
            firstName: selectedUser.firstName ?? '',
            lastName: selectedUser.lastName ?? '',
            email: selectedUser.email ?? '',
            phoneNumber: selectedUser.phoneNumber ?? '',
            age: selectedUser.age != null ? String(selectedUser.age) : '',
            gender: selectedUser.gender ?? '',
        });
        setEditing(true);
    };

    const cancelEdit = () => {
        setEditing(false);
        setUserForm(emptyUserForm);
    };

    const handleDelete = () => {
        if (!selectedUser || !window.confirm(`Delete ${selectedUser.username}?`)) return;
        run(async () => {
            await userApi.deleteUser(selectedUser.id);
            await loadUsers(null);
        });
    };

    const handleCreateEvent = (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedUser) return;
        run(async () => {
            await userApi.createEvent(selectedUser.externalUserId, {
                eventName: eventForm.eventName,
                eventDescription: eventForm.eventDescription || undefined,
                eventCategory: eventForm.eventCategory,
                eventLocation: eventForm.eventLocation || undefined,
                eventAttendance: Number(eventForm.eventAttendance) || 0,
            });
            setEventForm(emptyEventForm);
            await loadEvents(selectedUser.id);
        });
    };

    const userField = (key: keyof typeof emptyUserForm, label: string, type = 'text', required = false) => (
        <label style={labelStyle}>
            {label}
            <input
                type={type}
                value={userForm[key]}
                required={required}
                onChange={(e) => setUserForm({ ...userForm, [key]: e.target.value })}
            />
        </label>
    );

    return (
        <div style={{ width: '100%', minHeight: '100vh', fontFamily: 'sans-serif' }}>
            <header
                style={{
                    position: 'sticky',
                    top: 0,
                    zIndex: 100,
                    width: '100%',
                    boxSizing: 'border-box',
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
                        <Link to="/" style={{ textDecoration: 'none', fontWeight: 'bold' }}>
                            ← Home
                        </Link>
                        <strong>User Portal</strong>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <label>
                            Acting as User:{' '}
                            <select
                                value={selectedUserId ?? ''}
                                onChange={(e) => setSelectedUserId(e.target.value ? Number(e.target.value) : null)}
                                style={{ padding: '0.3rem' }}
                            >
                                {users.map((u) => (
                                    <option key={u.id} value={u.id}>
                                        {u.firstName} {u.lastName} ({u.username})
                                    </option>
                                ))}
                            </select>
                        </label>
                        <span style={{ fontSize: '0.9rem' }}>Signed in as <strong>{loggedInUser?.username}</strong></span>
                        <button onClick={handleLogout}>Log out</button>
                    </div>
                </div>
            </header>

            <main style={{ maxWidth: '1200px', margin: '0 auto', padding: '1.5rem 2rem' }}>
            {error && (
                <div style={{ padding: '1rem', border: '1px solid red', marginBottom: '1rem' }}>
                    {error}
                </div>
            )}

            {loading ? (
                <p>Fetching microservices data...</p>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                    <section>
                        <h2>{selectedUser ? 'Selected User' : 'No user selected'}</h2>
                        {selectedUser && (
                            <div style={boxStyle}>
                                <span style={tagStyle}>USER</span>
                                <h3 style={{ margin: '0.4rem 0' }}>
                                    {selectedUser.firstName} {selectedUser.lastName} (@{selectedUser.username})
                                </h3>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.25rem 1rem', fontSize: '0.9rem', color: '#555', margin: '0.5rem 0' }}>
                                    {selectedUser.email && <span>{selectedUser.email}</span>}
                                    {selectedUser.phoneNumber && <span>{selectedUser.phoneNumber}</span>}
                                    {selectedUser.age != null && <span>Age: {selectedUser.age}</span>}
                                    {selectedUser.gender && <span>Gender: {selectedUser.gender}</span>}
                                </div>
                                <p style={{ fontSize: '0.8rem', color: '#777' }}>UUID: {selectedUser.externalUserId}</p>
                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                    <button onClick={startEdit}>Edit</button>
                                    <button onClick={handleDelete}>Delete</button>
                                </div>
                            </div>
                        )}

                        <h2>{editing ? 'Edit User' : 'Create User'}</h2>
                        <form onSubmit={handleSaveUser} style={{ ...boxStyle, ...formStyle }}>
                            {userField('username', 'Username', 'text', true)}
                            {userField('password', editing ? 'New password (optional)' : 'Password', 'password', !editing)}
                            <div style={rowStyle}>
                                <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                                    {userField('firstName', 'First name')}
                                </div>
                                <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                                    {userField('lastName', 'Last name')}
                                </div>
                            </div>
                            {userField('email', 'Email', 'email')}
                            {userField('phoneNumber', 'Phone')}
                            <div style={rowStyle}>
                                <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                                    {userField('age', 'Age', 'number')}
                                </div>
                                <label style={{ ...labelStyle, flex: 1 }}>
                                    Gender
                                    <select
                                        value={userForm.gender}
                                        onChange={(e) => setUserForm({ ...userForm, gender: e.target.value })}
                                    >
                                        <option value="">Select gender</option>
                                        {GENDERS.map((g) => (
                                            <option key={g} value={g}>
                                                {g}
                                            </option>
                                        ))}
                                    </select>
                                </label>
                            </div>
                            <div style={rowStyle}>
                                <button type="submit" style={primaryBtn}>
                                    {editing ? 'Save changes' : 'Create user'}
                                </button>
                                {editing && (
                                    <button type="button" onClick={cancelEdit}>
                                        Cancel
                                    </button>
                                )}
                            </div>
                        </form>
                    </section>

                    <section>
                        <h2>Events ({events.length})</h2>
                        {events.length === 0 ? (
                            <p style={{ fontStyle: 'italic', fontSize: '0.9rem', color: '#666' }}>No events created.</p>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' }}>
                                {events.map((ev) => (
                                    <div key={ev.externalEventId} style={{ border: '1px solid #bbb', padding: '1rem' }}>
                                        <span style={{ ...tagStyle, backgroundColor: '#fef3c7' }}>COMMUNITY EVENT</span>
                                        <span style={{ fontSize: '0.8rem', color: '#555' }}>[{ev.eventCategory}]</span>
                                        <h3 style={{ margin: '0.4rem 0' }}>{ev.eventName}</h3>
                                        <p style={{ fontSize: '0.9rem', color: '#444', margin: 0 }}>
                                            Location: <strong>{ev.eventLocation}</strong> | Max Capacity:{' '}
                                            <strong>{ev.eventAttendance}</strong>
                                        </p>
                                    </div>
                                ))}
                            </div>
                        )}

                        {selectedUser && (
                            <>
                                <h2>Create Event</h2>
                                <form onSubmit={handleCreateEvent} style={{ ...boxStyle, ...formStyle }}>
                                    <input
                                        required
                                        placeholder="Event Name"
                                        value={eventForm.eventName}
                                        onChange={(e) => setEventForm({ ...eventForm, eventName: e.target.value })}
                                    />
                                    <textarea
                                        placeholder="Description"
                                        value={eventForm.eventDescription}
                                        onChange={(e) => setEventForm({ ...eventForm, eventDescription: e.target.value })}
                                    />
                                    <div style={rowStyle}>
                                        <select
                                            value={eventForm.eventCategory}
                                            onChange={(e) => setEventForm({ ...eventForm, eventCategory: e.target.value as EventCategory })}
                                        >
                                            {EVENT_CATEGORIES.map((c) => (
                                                <option key={c} value={c}>
                                                    {c}
                                                </option>
                                            ))}
                                        </select>
                                        <input
                                            placeholder="Location"
                                            style={{ flex: 1 }}
                                            value={eventForm.eventLocation}
                                            onChange={(e) => setEventForm({ ...eventForm, eventLocation: e.target.value })}
                                        />
                                        <input
                                            type="number"
                                            min={0}
                                            placeholder="Max Capacity"
                                            style={{ width: '110px' }}
                                            value={eventForm.eventAttendance}
                                            onChange={(e) => setEventForm({ ...eventForm, eventAttendance: e.target.value })}
                                        />
                                    </div>
                                    <div>
                                        <button type="submit" style={primaryBtn}>
                                            Create event
                                        </button>
                                    </div>
                                </form>
                            </>
                        )}
                    </section>
                </div>
            )}
            </main>
        </div>
    );
}
