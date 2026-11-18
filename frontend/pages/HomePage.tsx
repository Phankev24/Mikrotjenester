// @ts-ignore
import React from 'react';
import { Link } from 'react-router-dom';

export default function HomePage() {
    return (
        <div style={{ padding: '2rem' }}>
            <h1>Event Planning & Management</h1>
            <p>Select a workspace:</p>

            <ul style={{ listStyle: 'none', padding: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <li>
                    <Link to="/events">Events (Your Workspace)</Link>
                </li>
                <li>
                    <Link to="/vendor">Vendor (Your Workspace)</Link>
                </li>
                <li>
                    <Link to="/users">Users</Link>
                </li>
            </ul>
        </div>
    );
}