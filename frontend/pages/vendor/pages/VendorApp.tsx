import { useState } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage.tsx';
import RegisterPage from '../pages/RegisterPage.tsx';
import DashboardPage from '../pages/DashboardPage.tsx';
import VendorPage from '../pages/Vendor.tsx';
import type { LoginResponse } from '../types/Login';
import { GoogleOAuthProvider } from '@react-oauth/google';

const STORAGE_KEY = 'vendorSession';

export default function VendorApp() {
    const navigate = useNavigate();

    // Innloggingen overlever refresh, men forsvinner når fanen lukkes.
    const [session, setSession] = useState<LoginResponse | null>(() => {
        try {
            const raw = sessionStorage.getItem(STORAGE_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch {
            return null;
        }
    });
    const [notice, setNotice] = useState<string | null>(null);

    const handleLogin = (s: LoginResponse) => {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(s));
        setNotice(null);
        setSession(s);
        navigate('/vendor/dashboard');
    };

    const handleLogout = () => {
        sessionStorage.removeItem(STORAGE_KEY);
        setSession(null);
        navigate('/vendor/login');
    };

    // Henter Google Client ID trygt uten TypeScript-feil
    const googleClientId = (import.meta as any).env?.VITE_GOOGLE_CLIENT_ID ?? '';

    return (
        <GoogleOAuthProvider clientId={googleClientId}>
            <Routes>
                <Route index element={<VendorPage />} />

                <Route path="login" element={
                    <LoginPage
                        onLogin={handleLogin}
                        onRegister={() => navigate('/vendor/register')}
                        onShowVendors={() => navigate('/vendor')}
                        notice={notice}
                    />
                } />

                <Route path="register" element={
                    <RegisterPage
                        onRegistered={() => { setNotice('Kontoen er opprettet. Logg inn.'); navigate('/vendor/login'); }}
                        onBack={() => navigate('/vendor/login')}
                    />
                } />

                <Route path="dashboard" element={
                    session
                        ? <DashboardPage session={session} onLogout={handleLogout} />
                        : <Navigate to="/vendor/login" replace />
                } />

                <Route path="*" element={<Navigate to="/vendor" replace />} />
            </Routes>
        </GoogleOAuthProvider>
    );
}