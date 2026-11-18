import React, { useEffect, useState } from 'react';
import type { Vendor, VendorPatchRequest } from '../types/Vendor';

interface Props {
    vendor: Vendor;
    onSave: (patch: VendorPatchRequest) => Promise<void>;
}

const input: React.CSSProperties = { width: '100%', padding: '0.5rem', boxSizing: 'border-box' };

export default function ProfileForm({ vendor, onSave }: Props) {
    const [companyName, setCompanyName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [phone, setPhone] = useState('');
    const [website, setWebsite] = useState('');
    const [description, setDescription] = useState('');
    const [message, setMessage] = useState<string | null>(null);

    // Fyll skjemaet på nytt hver gang vendor-objektet endres (etter lasting og etter lagring)
    useEffect(() => {
        setCompanyName(vendor.companyName ?? '');
        setEmail(vendor.email ?? '');
        setPhone(vendor.phone ?? '');
        setWebsite(vendor.website ?? '');
        setDescription(vendor.description ?? '');
        setPassword('');
    }, [vendor]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setMessage(null);
        // PATCH: bare felter som er fylt ut (tom streng ville feilet valideringen)
        const patch: VendorPatchRequest = {};
        if (companyName.trim()) patch.companyName = companyName.trim();
        if (email.trim()) patch.email = email.trim();
        if (password) patch.password = password;
        if (phone.trim()) patch.phone = phone.trim();
        if (website.trim()) patch.website = website.trim();
        if (description.trim()) patch.description = description.trim();
        try {
            await onSave(patch);
            setMessage('Profilen er lagret');
        } catch (err) {
            setMessage(err instanceof Error ? err.message : 'Klarte ikke å lagre');
        }
    };

    return (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
            <label>Bedriftsnavn<input style={input} value={companyName} onChange={e => setCompanyName(e.target.value)} /></label>
            <label>E-post<input style={input} type="email" value={email} onChange={e => setEmail(e.target.value)} /></label>
            <label>Nytt passord (la stå tomt for å beholde)<input style={input} type="password" minLength={8} value={password} onChange={e => setPassword(e.target.value)} /></label>
            <label>Telefon<input style={input} value={phone} onChange={e => setPhone(e.target.value)} /></label>
            <label>Nettside<input style={input} value={website} onChange={e => setWebsite(e.target.value)} /></label>
            <label>Beskrivelse<textarea style={input} value={description} onChange={e => setDescription(e.target.value)} /></label>
            <button type="submit" style={{ background: '#2563eb', color: '#fff', padding: '0.6rem 1rem', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 'bold' }}>
                Lagre
            </button>
            {message && <p style={{ margin: 0 }}>{message}</p>}
        </form>
    );
}