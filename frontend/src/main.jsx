import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import HomePage from '../pages/HomePage';
import EventPage from '../pages/event/EventPage';
import UserPage from '../pages/user/user';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import { OAuthCallbackPage } from '../pages/auth/OAuthCallbackPage';
import ProtectedRoute from '../pages/auth/ProtectedRoute';
import { AuthProvider } from '../pages/auth/authContext';

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    {/*Delt Home page */}
                    <Route path="/" element={<HomePage />} />

                    {/* Isolert Event page */}
                    <Route path="/events/*" element={<EventPage />} />

                    {/* Auth */}
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/oauth-callback" element={<OAuthCallbackPage />} />

                    {/* User page (login required) */}
                    <Route
                        path="/users/*"
                        element={
                            <ProtectedRoute>
                                <UserPage />
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    </React.StrictMode>
);