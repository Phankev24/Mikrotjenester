import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import HomePage from '../pages/HomePage';
import EventPage from '../pages/event/EventPage';

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <BrowserRouter>
            <Routes>
                {/*Delt Home page */}
                <Route path="/" element={<HomePage />} />

                {/* Isolert Event page */}
                <Route path="/events/*" element={<EventPage />} />
            </Routes>
        </BrowserRouter>
    </React.StrictMode>
);