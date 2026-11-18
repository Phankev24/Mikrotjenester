import type { VendorEvent } from '../types/Event';

export default function EventList({ events }: { events: VendorEvent[] }) {
    if (events.length === 0) return null;
    return (
        <>
            <h3>Opprettet i denne økten</h3>
            <ul>
                {events.map(ev => (
                    <li key={ev.externalEventId}>
                        {ev.eventName} ({ev.eventCategory}, {ev.eventLocation || 'sted ikke satt'}, {ev.eventAttendance} personer)
                    </li>
                ))}
            </ul>
        </>
    );
}