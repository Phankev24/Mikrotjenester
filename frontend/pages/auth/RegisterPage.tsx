import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './authContext';
import { API_URL } from './auth';
import OAuthButtons from './OAuthButtons';
// @ts-ignore
import './authPages.css';

const GENDERS = ['Male', 'Female', 'Alien', 'Other'];

const RegisterPage: React.FC = () => {
    const [form, setForm] = useState({
        username: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
        age: '',
        gender: '',
    });
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login } = useAuth();

    const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
        setForm({ ...form, [key]: e.target.value });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (form.password !== form.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setLoading(true);

        try {
            const created = await fetch(`${API_URL}/user`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: form.username,
                    password: form.password,
                    firstName: form.firstName,
                    lastName: form.lastName,
                    email: form.email,
                    phoneNumber: form.phoneNumber,
                    age: Number(form.age),
                    gender: form.gender,
                }),
            });
            if (!created.ok) {
                throw new Error(
                    created.status === 409 || created.status === 500
                        ? 'Could not create account. That username may already be taken.'
                        : `Registration failed (${created.status})`
                );
            }

            const response = await fetch(`${API_URL}/user/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: form.username, password: form.password }),
            });
            if (!response.ok) throw new Error(`Account created, but login failed (${response.status})`);

            const data = await response.json();
            login(data.user, data.token);
            navigate('/users');
        } catch (err) {
            setError(
                err instanceof TypeError
                    ? 'Cannot reach the server. Make sure the gateway (8100) and user service are running.'
                    : err instanceof Error
                      ? err.message
                      : 'Something went wrong'
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                <Link to="/" className="auth-home-link">
                    ← Home
                </Link>
                <h1>Register</h1>

                {error && <div className="error-message">{error}</div>}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="username">Username</label>
                        <input id="username" required value={form.username} onChange={set('username')} disabled={loading} />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="firstName">First name</label>
                            <input id="firstName" required value={form.firstName} onChange={set('firstName')} disabled={loading} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="lastName">Last name</label>
                            <input id="lastName" required value={form.lastName} onChange={set('lastName')} disabled={loading} />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input type="email" id="email" required value={form.email} onChange={set('email')} disabled={loading} />
                    </div>

                    <div className="form-group">
                        <label htmlFor="phoneNumber">Phone</label>
                        <input type="tel" id="phoneNumber" required value={form.phoneNumber} onChange={set('phoneNumber')} disabled={loading} />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="age">Age</label>
                            <input type="number" id="age" min={0} max={150} required value={form.age} onChange={set('age')} disabled={loading} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="gender">Gender</label>
                            <select id="gender" required value={form.gender} onChange={set('gender')} disabled={loading}>
                                <option value="">Select gender</option>
                                {GENDERS.map((g) => (
                                    <option key={g} value={g}>
                                        {g}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input type="password" id="password" required minLength={6} value={form.password} onChange={set('password')} placeholder="At least 6 characters" disabled={loading} />
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirm password</label>
                        <input type="password" id="confirmPassword" required minLength={6} value={form.confirmPassword} onChange={set('confirmPassword')} disabled={loading} />
                    </div>

                    <button type="submit" className="submit-btn" disabled={loading}>
                        {loading ? 'Registering...' : 'Register'}
                    </button>
                </form>

                <div className="auth-divider">
                    <span>or</span>
                </div>

                <OAuthButtons verb="Sign up" disabled={loading} />

                <div className="auth-footer">
                    <p>
                        Already have an account? <Link to="/login">Sign in</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;
