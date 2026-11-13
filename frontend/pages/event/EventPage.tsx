import React from 'react';
import { Link } from 'react-router-dom';

export default function EventPage() {
    return (
        <div style={{ padding: '2rem' }}>
            <Link to="/">← Back to Home</Link>
            <h1>Event Management Workspace</h1>
            <p>Isolated route working. Ready to build the Event features.</p>
        </div>
    );
}