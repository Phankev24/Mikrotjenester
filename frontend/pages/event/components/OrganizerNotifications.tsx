import { useEffect, useState } from 'react';
import {EventJoinedNotificationDto} from "../types/event.ts";
import {eventApi} from "../api.eventApi.ts";

interface Props {
    organizerId: string;
    type: 'user' | 'vendor';
}

export function OrganizerNotifications({ organizerId, type }: Props) {
    const [notifications, setNotifications] = useState<EventJoinedNotificationDto[]>([]);

    useEffect(() => {
        if (!organizerId) return;

        let active = true;
        const fetchNotifications = async () => {
            const data =
                type === 'user'
                    ? await eventApi.getUserNotifications(organizerId)
                    : await eventApi.getVendorNotifications(organizerId);
            if (active) setNotifications(data);
        };

        fetchNotifications();
        const interval = setInterval(fetchNotifications, 3000); // polls every 3s for async updates
        return () => {
            active = false;
            clearInterval(interval);
        };
    }, [organizerId, type]);

    return (
        <div style={{
            border: '1px solid #e2e8f0',
            borderRadius: '8px',
            padding: '1rem',
            marginBottom: '1.5rem',
            backgroundColor: '#f8fafc'
        }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                <strong>Organizer Notifications (Kafka Async)</strong>
                <span style={{
                    backgroundColor: notifications.length > 0 ? '#2563eb' : '#94a3b8',
                    color: 'white',
                    borderRadius: '999px',
                    padding: '2px 8px',
                    fontSize: '0.75rem'
                }}>
                    {notifications.length}
                </span>
            </div>

            {notifications.length === 0 ? (
                <p style={{ margin: 0, color: '#64748b', fontSize: '0.9rem' }}>
                    No new attendee notifications yet.
                </p>
            ) : (
                <ul style={{ margin: 0, paddingLeft: '1.25rem' }}>
                    {notifications.map((n, idx) => (
                        <li key={`${n.externalEventId}-${n.attendeeUserId}-${idx}`} style={{ marginBottom: '0.25rem' }}>
                            <span>{n.message}</span>{' '}
                            <small style={{ color: '#64748b' }}>
                                ({new Date(n.joinedAt).toLocaleTimeString()})
                            </small>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}