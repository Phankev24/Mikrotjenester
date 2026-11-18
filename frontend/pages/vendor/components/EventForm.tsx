import React, { useState } from 'react';
import { EVENT_CATEGORIES, type EventCategory, type EventRequest } from '../types/Event';

interface Props {
    onSubmit: (body: EventRequest) => Promise<void>;
}

const input: React.CSSProperties = { width: '100%', padding: '0.5rem', boxSizing: 'border-box' };

export default function EventForm({ onSubmit }: Props) {
    const [eventName, setEventName] = useState('');
    const [eventDescription, setEventDescription] = useState('');
    const [eventCategory, setEventCategory] = useState<EventCategory>('DINNER');
    const [eventLocation, setEventLocation] = useState('');
    const [eventAttendance, setEventAttendance] = useState(0);
    const [message, setMessage] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setMessage(null);
        try {
            await onSubmit({ eventName, eventDescription, eventCategory, eventLocation, eventAttendance });
            setEventName('');
            setEventDescription('');
            setEventLocation('');
            setEventAttendance(0);
            setMessage('Eventet er opprettet');
        } catch (err) {
            setMessage(err instanceof Error ? err.message : 'Klarte ikke å opprette eventet');
        }
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
            <label>Navn<input style={input} value={eventName} onChange={e => setEventName(e.target.value)} required /></label>
            <label>Beskrivelse<input style={input} value={eventDescription} onChange={e => setEventDescription(e.target.value)} /></label>
            <label>Kategori
                <select style={input} value={eventCategory} onChange={e => setEventCategory(e.target.value as EventCategory)}>
                    {EVENT_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
            </label>
            <label>Sted<input style={input} value={eventLocation} onChange={e => setEventLocation(e.target.value)} /></label>
            <label>Antall deltakere<input style={input} type="number" min={0} value={eventAttendance} onChange={e => setEventAttendance(Number(e.target.value))} /></label>
            <button type="submit" style={{ background: '#2563eb', color: '#fff', padding: '0.6rem 1rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                Opprett event
            </button>
            {message && <p style={{ margin: 0 }}>{message}</p>}
        </form>
    );
}