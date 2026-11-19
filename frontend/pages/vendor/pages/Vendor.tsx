import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { vendorApi } from '../services/VendorApi';
import type { Vendor } from '../types/Vendor';

const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '0.5rem',
    boxSizing: 'border-box',
    marginTop: '0.25rem',
};

export default function VendorPage() {
    const navigate = useNavigate();
    const [vendors, setVendors] = useState<Vendor[]>([]);
    const [companyName, setCompanyName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [phone, setPhone] = useState('');
    const [website, setWebsite] = useState('');
    const [description, setDescription] = useState('');
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const loadVendors = async () => {
        setError(null);
        try {
            const data = await vendorApi.getAllVendors();
            setVendors(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Failed to load vendors:', err);
            setError(err instanceof Error ? err.message : 'Kunne ikke hente vendorer');
        }
    };

    useEffect(() => {
        loadVendors();
    }, []);

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        setMessage(null);
        setError(null);
        try {
            await vendorApi.register({ companyName, email, password, phone, website, description });
            setCompanyName('');
            setEmail('');
            setPassword('');
            setPhone('');
            setWebsite('');
            setDescription('');
            setMessage('Vendor opprettet');
            await loadVendors();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Kunne ikke opprette vendor');
        }
    };

    const handleMainMenu = () => {
        window.location.href = 'http://localhost:5173';
    };

    return (
        <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: 900, margin: '0 auto' }}>
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h1 style={{ margin: 0 }}>Vendor Administrasjon</h1>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button
                        type="button"
                        onClick={handleMainMenu}
                        style={{ background: '#4b5563', color: '#fff', padding: '0.6rem 1rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}
                    >
                        Hovedmeny
                    </button>
                    <button
                        type="button"
                        onClick={() => navigate('/vendor/login')}
                        style={{ background: '#2563eb', color: '#fff', padding: '0.6rem 1rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}
                    >
                        Til innlogging
                    </button>
                </div>
            </header>

            <section style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, border: '1px solid #e5e7eb', marginBottom: '2rem' }}>
                <h2 style={{ marginTop: 0 }}>Registrer ny Vendor (POST /api/vendors)</h2>
                <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
                    <label>Bedriftsnavn:
                        <input style={inputStyle} value={companyName} onChange={e => setCompanyName(e.target.value)} required />
                    </label>
                    <label>E-post:
                        <input style={inputStyle} type="email" value={email} onChange={e => setEmail(e.target.value)} required />
                    </label>
                    <label>Passord (8-72 tegn):
                        <input style={inputStyle} type="password" minLength={8} maxLength={72} value={password} onChange={e => setPassword(e.target.value)} required />
                    </label>
                    <label>Telefon:
                        <input style={inputStyle} value={phone} onChange={e => setPhone(e.target.value)} />
                    </label>
                    <label>Nettside:
                        <input style={inputStyle} value={website} onChange={e => setWebsite(e.target.value)} />
                    </label>
                    <label>Beskrivelse:
                        <textarea style={inputStyle} rows={3} value={description} onChange={e => setDescription(e.target.value)} />
                    </label>

                    {message && <p style={{ color: '#15803d', margin: 0 }}>{message}</p>}
                    {error && <p style={{ color: '#b91c1c', margin: 0 }}>{error}</p>}

                    <button type="submit" style={{ background: '#28a745', color: '#fff', padding: '0.75rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                        Lagre Vendor
                    </button>
                </form>
            </section>

            <section>
                <h2>Eksisterende Vendorer (GET /api/vendors)</h2>
                {vendors.length === 0 ? (
                    <p style={{ color: '#6b7280' }}>Ingen vendorer funnet.</p>
                ) : (
                    <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', paddingLeft: '1.2rem' }}>
                        {vendors.map((v, idx) => (
                            <li key={v.externalId ?? idx}>
                                <strong>{v.companyName}</strong> ({v.email})
                            </li>
                        ))}
                    </ul>
                )}
            </section>
        </div>
    );
}