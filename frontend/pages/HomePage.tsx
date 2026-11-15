// @ts-ignore
import React from 'react';
import { Link } from 'react-router-dom';

export default function HomePage() {
    return (
        <div style={{ padding: '2rem' }}>
            <h1>Event Planning & Management</h1>
            <p>Select a workspace:</p>

            <nav style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <Link to="/events">Events (Your Workspace)</Link>
                {/* <Link to="/users">Users</Link> */}
                {/* <Link to="/vendors">Vendors</Link> */}
            </nav>
        </div>
    );
}