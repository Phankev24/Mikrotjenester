import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { userApi } from './userApi';
import { useAuth } from '../auth/authContext';
import { EventDto, UserResponseDto } from './types/user';
// @ts-ignore
import './user.css';

const GENDERS = ['Male', 'Female', 'Alien', 'Other'];

const emptyUserForm = { username: '', password: '', firstName: '', lastName: '', email: '', phoneNumber: '', age: '', gender: '' };

export default function UserPage() {
    const navigate = useNavigate();
    const { user: loggedInUser, token, login, logout } = useAuth();

    const [isAdminMode, setIsAdminMode] = useState<boolean>(
        localStorage.getItem('epm_role') === 'SENSOR'
    );
    const [users, setUsers] = useState<UserResponseDto[]>([]);
    const [selectedUserId, setSelectedUserId] = useState<number | null>(loggedInUser?.id ?? null);

    const handleLogout = () => {
        localStorage.removeItem('epm_role');
        logout();
        navigate('/login');
    };

    const handleGoToEvents = () => {
        if (!isAdminMode) {
            localStorage.removeItem('epm_role');
        }
        navigate('/events');
    };

    const toggleAdminMode = () => {
        if (isAdminMode) {
            localStorage.removeItem('epm_role');
            setIsAdminMode(false);
            setSelectedUserId(loggedInUser?.id ?? null);
        } else {
            localStorage.setItem('epm_role', 'SENSOR');
            setIsAdminMode(true);
        }
    };

    const [events, setEvents] = useState<EventDto[]>([]);
    const [joinedEventIds, setJoinedEventIds] = useState<Set<string>>(new Set());

    const [userForm, setUserForm] = useState(emptyUserForm);
    const [editing, setEditing] = useState<boolean>(false);

    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // In normal mode, lock selection to loggedInUser; in Admin mode, allow inspecting any user
    const effectiveUserId = isAdminMode ? selectedUserId : (loggedInUser?.id ?? selectedUserId);
    const selectedUser = users.find((u) => u.id === effectiveUserId) ?? null;
    const isOwnProfile = selectedUser !== null && selectedUser.id === loggedInUser?.id;

    const loadUsers = async (keepSelection?: number | null) => {
        setLoading(true);
        setError(null);
        try {
            const data = await userApi.getUsers();
            if (loggedInUser && !data.some((u) => u.id === loggedInUser.id)) {
                logout();
                navigate('/login');
                return;
            }
            setUsers(data);
            const target = keepSelection ?? effectiveUserId;
            setSelectedUserId(data.some((u) => u.id === target) ? target : (loggedInUser?.id ?? data[0]?.id ?? null));
        } catch (err) {
            console.error(err);
            setError('Failed to load users. Ensure the User service (8081) is running.');
        } finally {
            setLoading(false);
        }
    };

    const loadEvents = async (user: UserResponseDto) => {
        try {
            const [created, allEvents, joinedIds] = await Promise.all([
                userApi.getUserWithEvents(user.id),
                userApi.getAllEvents(),
                userApi.getAttendingEventIds(user.externalUserId),
            ]);
            const createdIds = new Set(created.events.map((e) => e.externalEventId));
            const joined = new Set(joinedIds);
            setJoinedEventIds(joined);
            setEvents(allEvents.filter((e) => createdIds.has(e.externalEventId) || joined.has(e.externalEventId)));
        } catch (err) {
            console.error(err);
            setEvents([]);
            setJoinedEventIds(new Set());
            setError('Failed to load events. Ensure the Event service (8080) is running.');
        }
    };

    useEffect(() => {
        loadUsers();
    }, []);

    useEffect(() => {
        setEditing(false);
        setUserForm(emptyUserForm);
    }, [effectiveUserId]);

    const selectedUserExists = selectedUser !== null;

    useEffect(() => {
        if (selectedUser) loadEvents(selectedUser);
        else {
            setEvents([]);
            setJoinedEventIds(new Set());
        }
    }, [effectiveUserId, selectedUserExists]);

    useEffect(() => {
        const refresh = () => {
            if (selectedUser && document.visibilityState === 'visible') loadEvents(selectedUser);
        };
        window.addEventListener('focus', refresh);
        document.addEventListener('visibilitychange', refresh);
        return () => {
            window.removeEventListener('focus', refresh);
            document.removeEventListener('visibilitychange', refresh);
        };
    }, [selectedUser]);

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

    const handleSaveProfile = (e: React.FormEvent) => {
        e.preventDefault();
        if (!editing || !selectedUser) return;
        run(async () => {
            const { password, ...rest } = toUserDto();
            const updated = await userApi.updateUser(selectedUser.id, password ? { ...rest, password } : rest);
            if (token && updated.id === loggedInUser?.id) login(updated, token);
            setEditing(false);
            setUserForm(emptyUserForm);
            await loadUsers(selectedUser.id);
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
            const deletedOwnAccount = selectedUser.id === loggedInUser?.id;
            await userApi.deleteUser(selectedUser.id);
            if (deletedOwnAccount) {
                handleLogout();
                return;
            }
            await loadUsers(loggedInUser?.id ?? null);
        });
    };

    const userField = (key: keyof typeof emptyUserForm, label: string, type = 'text', required = false) => (
        <label className="user-label">
            {label}
            <input
                type={type}
                value={userForm[key]}
                required={required}
                onChange={(e) => setUserForm({ ...userForm, [key]: e.target.value })}
            />
        </label>
    );

    const infoItem = (label: string, value: string | number | null | undefined) => (
        <div>
            <div className="user-info-label">{label}</div>
            <div className="user-info-value">
                {value != null && value !== '' ? value : <span className="user-info-empty">Not set</span>}
            </div>
        </div>
    );

    return (
        <div className="user-page">
            <header className="user-header">
                <div className="user-header-inner">
                    <div className="user-header-left">
                        <Link to="/" className="user-home-link">
                            ← Home
                        </Link>
                        <strong>User Portal</strong>
                        <button
                            type="button"
                            className="user-primary-btn"
                            onClick={handleGoToEvents}
                            style={{ marginLeft: '0.75rem' }}
                        >
                            View / Create Events →
                        </button>
                    </div>
                    <div className="user-header-right">
                        {/* Only show account switcher in Admin / Sensor Mode */}
                        {isAdminMode && (
                            <label>
                                Acting as User:{' '}
                                <select
                                    className="user-picker-select"
                                    value={effectiveUserId ?? ''}
                                    onChange={(e) => setSelectedUserId(e.target.value ? Number(e.target.value) : null)}
                                >
                                    {users.map((u) => (
                                        <option key={u.id} value={u.id}>
                                            {u.firstName} {u.lastName} ({u.username})
                                        </option>
                                    ))}
                                </select>
                            </label>
                        )}

                        <span className="user-signed-in">
                            Signed in as <strong>{loggedInUser?.username}</strong>
                        </span>

                        <button
                            type="button"
                            onClick={toggleAdminMode}
                            style={{
                                padding: '0.35rem 0.7rem',
                                fontSize: '0.8rem',
                                backgroundColor: isAdminMode ? '#dc2626' : '#1e293b',
                                color: '#fff',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer',
                            }}
                        >
                            {isAdminMode ? 'Exit Admin Mode' : 'Admin Mode'}
                        </button>

                        <button onClick={handleLogout}>Log out</button>
                    </div>
                </div>
            </header>

            <main className="user-main">
                {isAdminMode && (
                    <div
                        style={{
                            padding: '0.65rem 1rem',
                            backgroundColor: '#fef3c7',
                            border: '1px solid #f59e0b',
                            borderRadius: '6px',
                            marginBottom: '1rem',
                            fontSize: '0.9rem',
                            color: '#92400e',
                        }}
                    >
                        <strong>Admin / Sensor Mode active:</strong> You can inspect, edit, or delete any user account from the top dropdown.
                    </div>
                )}

                {error && <div className="user-error">{error}</div>}

                {loading ? (
                    <p>Fetching microservices data...</p>
                ) : (
                    <>
                        <section>
                            {selectedUser ? (
                                <div className="user-profile">
                                    <div className="user-profile-top">
                                        <div>
                                            <span className="user-tag">{isOwnProfile ? 'YOU' : 'USER'}</span>
                                            <h1 className="user-profile-name">
                                                {selectedUser.firstName} {selectedUser.lastName}
                                            </h1>
                                            <span className="user-handle">@{selectedUser.username}</span>
                                        </div>
                                        <div className="user-actions">
                                            {(isOwnProfile || isAdminMode) && !editing && (
                                                <button className="user-primary-btn" onClick={startEdit}>
                                                    Edit profile
                                                </button>
                                            )}
                                            {(isOwnProfile || isAdminMode) && (
                                                <button className="user-danger-btn" onClick={handleDelete}>
                                                    Delete
                                                </button>
                                            )}
                                        </div>
                                    </div>

                                    <div className="user-info">
                                        {infoItem('Email', selectedUser.email)}
                                        {infoItem('Phone', selectedUser.phoneNumber)}
                                        {infoItem('Age', selectedUser.age)}
                                        {infoItem('Gender', selectedUser.gender)}
                                    </div>

                                    <p className="user-uuid">UUID: {selectedUser.externalUserId}</p>
                                </div>
                            ) : (
                                <h2>No user selected</h2>
                            )}
                        </section>

                        {editing && selectedUser && (
                            <section className="user-edit">
                                <h3>Edit Profile</h3>
                                <form onSubmit={handleSaveProfile} className="user-edit-form">
                                    <div className="user-edit-grid">
                                        {userField('username', 'Username', 'text', true)}
                                        {userField('password', 'New password (optional)', 'password')}
                                        {userField('firstName', 'First name')}
                                        {userField('lastName', 'Last name')}
                                        {userField('email', 'Email', 'email')}
                                        {userField('phoneNumber', 'Phone')}
                                        {userField('age', 'Age', 'number')}
                                        <label className="user-label">
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
                                    <div className="user-actions">
                                        <button type="submit" className="user-primary-btn">
                                            Save changes
                                        </button>
                                        <button type="button" onClick={cancelEdit}>
                                            Cancel
                                        </button>
                                    </div>
                                </form>
                            </section>
                        )}

                        <section>
                            <div
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    marginBottom: '0.75rem',
                                }}
                            >
                                <h2 style={{ margin: 0 }}>Events ({events.length})</h2>
                                <button
                                    type="button"
                                    className="user-primary-btn"
                                    onClick={handleGoToEvents}
                                >
                                    + Create / Browse Events
                                </button>
                            </div>

                            {events.length === 0 ? (
                                <p className="user-empty">No events joined or created.</p>
                            ) : (
                                <div className="user-events">
                                    {events.map((ev) => (
                                        <div key={ev.externalEventId} className="user-event-card">
                                            {ev.externalUserId === selectedUser?.externalUserId && (
                                                <span className="user-tag user-tag-event">CREATED</span>
                                            )}
                                            {joinedEventIds.has(ev.externalEventId) && (
                                                <span className="user-tag">ATTENDING</span>
                                            )}
                                            <span className="user-event-category">[{ev.eventCategory}]</span>
                                            <h3 className="user-event-name">{ev.eventName}</h3>
                                            <p className="user-event-text">
                                                Location: <strong>{ev.eventLocation}</strong> | Max Capacity:{' '}
                                                <strong>{ev.eventAttendance}</strong>
                                            </p>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </section>
                    </>
                )}
            </main>
        </div>
    );
}