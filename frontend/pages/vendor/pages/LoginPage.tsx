import React, { useState } from 'react';
import { GoogleLogin } from '@react-oauth/google';
import { ApiError } from '../../../src/api/api';
import { vendorApi } from '../services/VendorApi';
import type { LoginResponse } from '../types/Login';

interface Props {
    onLogin: (s: LoginResponse) => void;
    onRegister: () => void;
    onShowVendors: () => void;
    notice: string | null;
}

const input: React.CSSProperties = { width: '100%', padding: '0.6rem', boxSizing: 'border-box' };
const linkButton: React.CSSProperties = { background: 'none', border: 'none', color: '#2563eb', cursor: 'pointer' };

export default function LoginPage({ onLogin, onRegister, onShowVendors, notice }: Props) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setBusy(true);
        setError(null);
        try {
            onLogin(await vendorApi.login({ email, password }));
        } catch (err) {
            if (err instanceof ApiError && err.status === 401) setError('Feil e-post eller passord');
            else setError(err instanceof Error ? err.message : 'Innlogging feilet');
        } finally {
            setBusy(false);
        }
    };

    const handleGoogle = async (idToken?: string) => {
        if (!idToken) { setError('Google ga ikke noe token'); return; }
        setError(null);
        try {
            onLogin(await vendorApi.googleLogin(idToken));
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Google-innlogging feilet');
        }
    };

    return (
        <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', fontFamily: 'sans-serif', background: '#f3f4f6' }}>
            <form onSubmit={handleSubmit} style={{ background: '#fff', padding: '2rem', borderRadius: 8, width: 340, boxShadow: '0 2px 10px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h1 style={{ margin: 0 }}>Vendor-innlogging</h1>
                {notice && <p style={{ color: '#15803d', margin: 0 }}>{notice}</p>}
                <label>E-post
                    <input type="email" value={email} onChange={e => setEmail(e.target.value)} required autoFocus style={input} />
                </label>
                <label>Passord
                    <input type="password" value={password} onChange={e => setPassword(e.target.value)} required style={input} />
                </label>
                {error && <p style={{ color: '#b91c1c', margin: 0 }}>{error}</p>}
                <button type="submit" disabled={busy} style={{ background: '#2563eb', color: '#fff', padding: '0.75rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                    {busy ? 'Logger inn...' : 'Logg inn'}
                </button>

                <div style={{ textAlign: 'center', color: '#666' }}>eller</div>
                <div style={{ display: 'flex', justifyContent: 'center' }}>
                    <GoogleLogin
                        onSuccess={cred => handleGoogle(cred.credential)}
                        onError={() => setError('Google-innlogging feilet')}
                    />
                </div>

                <button type="button" onClick={onRegister} style={linkButton}>Ny vendor? Registrer deg</button>
                <button type="button" onClick={onShowVendors} style={linkButton}>Se alle vendorer</button>
            </form>
        </div>
    );
}