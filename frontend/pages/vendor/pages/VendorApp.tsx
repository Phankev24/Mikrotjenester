import { useState } from 'react';
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage.tsx';
import RegisterPage from '../pages/RegisterPage.tsx';
import DashboardPage from '../pages/DashboardPage.tsx';
import VendorPage from '../pages/Vendor.tsx';
import type { LoginResponse } from '../types/Login';

const STORAGE_KEY = 'vendorSession';

export default function VendorApp() {
    const navigate = useNavigate();

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

    return (
        <Routes>
            <Route path="login" element={
                <LoginPage
                    onLogin={handleLogin}
                    onRegister={() => navigate('/vendor/register')}
                    onShowVendors={() => navigate('/vendor/list')}
                    notice={notice}
                />
            } />

            <Route path="register" element={
                <RegisterPage
                    onRegistered={() => { setNotice('Kontoen er opprettet. Logg inn.'); navigate('/vendor/login'); }}
                    onBack={() => navigate('/vendor/login')}
                />
            } />

            {/* Admin/vendor-sideoversikt ligger her */}
            <Route path="list" element={<VendorPage />} />

            <Route path="dashboard" element={
                session
                    ? <DashboardPage session={session} onLogout={handleLogout} onBackToVendors={() => navigate('/vendor/list')} />
                    : <Navigate to="/vendor/login" replace />
            } />

            {/* Standardrute sender til registrer vendor først */}
            <Route index element={<Navigate to="/vendor/register" replace />} />
            <Route path="*" element={<Navigate to="/vendor/register" replace />} />
        </Routes>
    );
}