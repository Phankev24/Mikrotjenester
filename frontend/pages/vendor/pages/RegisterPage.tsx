import React, { useState } from 'react';
import { vendorApi } from '../services/VendorApi.ts';

interface Props {
    onRegistered: () => void;
    onBack: () => void;
}

const input: React.CSSProperties = { width: '100%', padding: '0.6rem', boxSizing: 'border-box' };

export default function RegisterPage({ onRegistered, onBack }: Props) {
    const [companyName, setCompanyName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [phone, setPhone] = useState('');
    const [website, setWebsite] = useState('');
    const [description, setDescription] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        // Valider passordlengde (8 - 72 tegn)
        if (password.length < 8 || password.length > 72) {
            setError('Passordet må være mellom 8 og 72 tegn.');
            return;
        }

        // Sjekk om passordene matcher
        if (password !== confirmPassword) {
            setError('Passordene er ikke like.');
            return;
        }

        setBusy(index => true);
        try {
            await vendorApi.register({ companyName, email, password, phone, website, description });
            onRegistered();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Registrering feilet');
        } finally {
            setBusy(false);
        }
    };

    return (
        <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', fontFamily: 'sans-serif', background: '#f3f4f6', padding: '1rem' }}>
            <form onSubmit={handleSubmit} style={{ background: '#fff', padding: '2rem', borderRadius: 8, width: 400, boxShadow: '0 2px 10px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
                <h1 style={{ margin: 0 }}>Registrer vendor</h1>
                <label>Bedriftsnavn<input style={input} value={companyName} onChange={e => setCompanyName(e.target.value)} required /></label>
                <label>E-post<input style={input} type="email" value={email} onChange={e => setEmail(e.target.value)} required /></label>

                <label>Passord (8-72 tegn)
                    <input style={input} type="password" minLength={8} maxLength={72} value={password} onChange={e => setPassword(e.target.value)} required />
                </label>

                <label>Bekreft passord
                    <input style={input} type="password" minLength={8} maxLength={72} value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
                </label>

                <label>Telefon<input style={input} value={phone} onChange={e => setPhone(e.target.value)} /></label>
                <label>Nettside<input style={input} value={website} onChange={e => setWebsite(e.target.value)} /></label>
                <label>Beskrivelse<textarea style={input} value={description} onChange={e => setDescription(e.target.value)} /></label>

                {error && <p style={{ color: '#b91c1c', margin: 0 }}>{error}</p>}

                <button type="submit" disabled={busy} style={{ background: '#28a745', color: '#fff', padding: '0.75rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                    {busy ? 'Lagrer...' : 'Opprett konto'}
                </button>
                <button type="button" onClick={onBack} style={{ background: 'none', border: 'none', color: '#2563eb', cursor: 'pointer' }}>
                    Tilbake til innlogging
                </button>
            </form>
        </div>
    );
}