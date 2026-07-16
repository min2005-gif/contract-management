import axios from 'axios';

/** Shared axios instance. Requests are proxied to the backend by Vite in dev (see vite.config.ts). */
export const api = axios.create({ baseURL: '/' });

const TOKEN_KEY = 'vatm.auth';

export function setStoredToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

// Attach the bearer token to every request.
api.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401 (expired/invalid token), clear the session and return to the login page.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      setStoredToken(null);
      localStorage.removeItem('vatm.profile');
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);

/** Extracts a human-readable message from an RFC 7807 problem+json error. */
export function errorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const detail = err.response?.data?.detail;
    if (typeof detail === 'string') return detail;
    if (err.response?.status === 401) return 'Phiên đăng nhập đã hết hạn.';
    return err.message;
  }
  return 'Đã xảy ra lỗi không xác định.';
}
