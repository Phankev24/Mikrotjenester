import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import HomePage from '../pages/HomePage';
import EventPage from '../pages/event/EventPage';
import VendorApp from '../pages/vendor/pages/VendorApp'

ReactDOM.createRoot(getElementByIdOrThrow('root')).render(
    <React.StrictMode>
        <BrowserRouter>
            <Routes>
                {/* Delt Home page */}
                <Route path="/" element={<HomePage />} />

                {/* Isolert Event page */}
                <Route path="/events/*" element={<EventPage />} />

                {/* Vendor-modulen har egne underruter */}
                <Route path="/vendor/*" element={<VendorApp />} />
            </Routes>
        </BrowserRouter>
    </React.StrictMode>
);

function getElementByIdOrThrow(id) {
    const el = document.getElementById(id);
    if (!el) throw new Error(`Root element with id '${id}' not found`);
    return el;
}