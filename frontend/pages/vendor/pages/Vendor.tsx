import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface Vendor {
    externalId: string;
    companyName: string;
    email: string;
    phone: string;
    website: string;
    description: string;
}

export default function VendorPage() {
    const navigate = useNavigate();
    const [vendors, setVendors] = useState<Vendor[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Skjemastate for ny vendor
    const [companyName, setCompanyName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [phone, setPhone] = useState('');
    const [website, setWebsite] = useState('');
    const [description, setDescription] = useState('');

    // Hent alle vendorer (GET)
    const fetchVendors = async () => {
        try {
            setLoading(true);
            const response = await fetch('http://localhost:8082/api/vendors');
            if (!response.ok) throw new Error('Klarte ikke å hente vendorer fra serveren');
            const data = await response.json();
            setVendors(data);
            setError(null);
        } catch (err: any) {
            setError(err.message || 'Noe gikk galt');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchVendors();
    }, []);

    // Opprett ny vendor (POST)
    const handleCreateVendor = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await fetch('http://localhost:8082/api/vendors', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    companyName,
                    email,
                    password,
                    phone,
                    website,
                    description
                }),
            });

            if (!response.ok) throw new Error('Klarte ikke å opprette vendor');

            // Nullstill skjemaet og oppdater listen
            setCompanyName('');
            setEmail('');
            setPassword('');
            setPhone('');
            setWebsite('');
            setDescription('');
            fetchVendors();
        } catch (err: any) {
            alert(err.message);
        }
    };

    return (
        <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: '800px', margin: '0 auto' }}>
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1>Vendor Administrasjon</h1>
                <button
                    type="button"
                    onClick={() => navigate('/vendor/login')}
                    style={{ background: '#2563eb', color: 'white', padding: '0.6rem 1rem', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                >
                    Til innlogging
                </button>
            </header>

            {/* Skjema for å legge til vendor */}
            <div style={{ background: '#f9f9f9', padding: '1.5rem', borderRadius: '8px', marginBottom: '2rem', border: '1px solid #ddd' }}>
                <h2>Registrer ny Vendor (POST /api/vendors)</h2>
                <form onSubmit={handleCreateVendor} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div>
                        <label>Bedriftsnavn:</label><br />
                        <input type="text" value={companyName} onChange={e => setCompanyName(e.target.value)} required style={{ width: '100%', padding: '0.5rem' }} />
                    </div>
                    <div>
                        <label>E-post:</label><br />
                        <input type="email" value={email} onChange={e => setEmail(e.target.value)} required style={{ width: '100%', padding: '0.5rem' }} />
                    </div>
                    <div>
                        <label>Passord:</label><br />
                        <input type="password" value={password} onChange={e => setPassword(e.target.value)} required style={{ width: '100%', padding: '0.5rem' }} />
                    </div>
                    <div>
                        <label>Telefon:</label><br />
                        <input type="text" value={phone} onChange={e => setPhone(e.target.value)} style={{ width: '100%', padding: '0.5rem' }} />
                    </div>
                    <div>
                        <label>Nettside:</label><br />
                        <input type="text" value={website} onChange={e => setWebsite(e.target.value)} style={{ width: '100%', padding: '0.5rem' }} />
                    </div>
                    <div>
                        <label>Beskrivelse:</label><br />
                        <textarea value={description} onChange={e => setDescription(e.target.value)} style={{ width: '100%', padding: '0.5rem' }} />
                    </div>
                    <button type="submit" style={{ background: '#28a745', color: 'white', padding: '0.75rem', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                        Lagre Vendor
                    </button>
                </form>
            </div>

            {/* Liste over eksisterende vendorer */}
            <h2>Eksisterende Vendorer (GET /api/vendors)</h2>
            {loading && <p>Laster vendorer...</p>}
            {error && <p style={{ color: 'red' }}>Feil ved henting: {error}</p>}
            {!loading && !error && vendors.length === 0 && <p>Ingen vendorer funnet.</p>}

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {vendors.map(v => (
                    <div key={v.externalId} style={{ border: '1px solid #ccc', padding: '1rem', borderRadius: '6px', background: '#fff' }}>
                        <h3>{v.companyName}</h3>
                        <p><strong>E-post:</strong> {v.email}</p>
                        <p><strong>Telefon:</strong> {v.phone}</p>
                        <p><strong>Nettside:</strong> <a href={v.website} target="_blank" rel="noreferrer">{v.website}</a></p>
                        <p><strong>Beskrivelse:</strong> {v.description}</p>
                        <p style={{ fontSize: '0.8rem', color: '#666' }}>ID: {v.externalId}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}