import React, { createContext, useContext, useState } from 'react';
import { UserResponseDto } from '../user/types/user';
import { auth } from './auth';

interface AuthContextType {
    user: UserResponseDto | null;
    token: string | null;
    isAuthenticated: boolean;
    login: (user: UserResponseDto, token: string) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<UserResponseDto | null>(() => auth.getUser());
    const [token, setToken] = useState<string | null>(() => auth.getToken());

    const login = (newUser: UserResponseDto, newToken: string) => {
        auth.setSession(newToken, newUser);
        setUser(newUser);
        setToken(newToken);
    };

    const logout = () => {
        auth.logout();
        setUser(null);
        setToken(null);
    };

    const isAuthenticated = !!user && !!token;

    return (
        <AuthContext.Provider value={{ user, token, isAuthenticated, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
