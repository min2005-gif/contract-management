import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { api, getStoredToken, setStoredToken } from '../api/client';
import type { AuthProfile } from '../api/types';

interface AuthState {
  profile: AuthProfile | null;
  login: (subject: string, unit: string, roles: string[]) => Promise<void>;
  logout: () => void;
}

const PROFILE_KEY = 'vatm.profile';
const AuthCtx = createContext<AuthState | null>(null);

function loadProfile(): AuthProfile | null {
  const token = getStoredToken();
  const raw = localStorage.getItem(PROFILE_KEY);
  if (!token || !raw) return null;
  try {
    return { ...(JSON.parse(raw) as AuthProfile), token };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [profile, setProfile] = useState<AuthProfile | null>(loadProfile);

  const value = useMemo<AuthState>(
    () => ({
      profile,
      async login(subject, unit, roles) {
        const { data } = await api.post<AuthProfile>('/api/v1/dev/token', {
          subject,
          unit,
          roles,
          name: subject,
        });
        setStoredToken(data.token);
        localStorage.setItem(PROFILE_KEY, JSON.stringify(data));
        setProfile(data);
      },
      logout() {
        setStoredToken(null);
        localStorage.removeItem(PROFILE_KEY);
        setProfile(null);
      },
    }),
    [profile],
  );

  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthCtx);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
