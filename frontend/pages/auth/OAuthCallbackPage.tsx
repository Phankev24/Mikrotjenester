import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from './authContext';
import { API_URL } from './auth';

export function OAuthCallbackPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const { login } = useAuth();
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const handleOAuthCallback = async () => {
            const token = new URLSearchParams(location.search).get('token');

            if (!token) {
                setError('Invalid login data');
                setTimeout(() => navigate('/login'), 2000);
                return;
            }

            try {
                const response = await fetch(`${API_URL}/user/me`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (!response.ok) throw new Error('Login failed');

                login(await response.json(), token);
                navigate('/users');
            } catch (err) {
                console.error('OAuth login error:', err);
                setError(err instanceof Error ? err.message : 'Login failed');
                setTimeout(() => navigate('/login'), 3000);
            }
        };

        handleOAuthCallback();
    }, [location.search, navigate, login]);

    if (error) {
        return (
            <div className="auth-page">
                <p>Error: {error}. Redirecting back...</p>
            </div>
        );
    }

    return (
        <div className="auth-page">
            <p>Signing you in, one moment...</p>
        </div>
    );
}
