import React from 'react';
import { API_URL } from './auth';

type Provider = 'google' | 'github';

const ENABLED: Record<Provider, boolean> = {
    google: true,
    github: true,
};

interface OAuthButtonsProps {
    disabled?: boolean;
    verb: string;
}

const OAuthButtons: React.FC<OAuthButtonsProps> = ({ disabled, verb }) => {
    const handleOAuthLogin = (provider: Provider) => {
        window.location.href = `${API_URL}/user/oauth2/authorization/${provider}`;
    };

    const button = (provider: Provider, label: string) => (
        <button
            type="button"
            onClick={() => handleOAuthLogin(provider)}
            className={`oauth-btn ${provider}-btn`}
            disabled={disabled || !ENABLED[provider]}
            title={ENABLED[provider] ? undefined : 'Coming soon'}
        >
            <img src={`/icons/${provider}.svg`} alt="" width={20} height={20} />
            {verb} with {label}
            {ENABLED[provider] ? '' : ' (coming soon)'}
        </button>
    );

    return (
        <div className="oauth-buttons">
            {button('google', 'Google')}
            {button('github', 'GitHub')}
        </div>
    );
};

export default OAuthButtons;
