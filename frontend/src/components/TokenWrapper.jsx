import React, { useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

export default function TokenWrapper({ children }) {
  const [status, setStatus] = useState('loading'); // 'loading' | 'authorized' | 'unauthorized'

  useEffect(() => {
    const checkTokens = async () => {
      const accessToken = sessionStorage.getItem('accessToken');
      const refreshToken = sessionStorage.getItem('refreshToken');

      const isAccessValid = (token) => {
        if (!token) return false;
        try {
          const decoded = jwtDecode(token);
          if (!decoded || !decoded.exp) return true; // mocked tokens may not have exp
          return Date.now() < decoded.exp * 1000;
        } catch (e) {
          return false;
        }
      };

      if (isAccessValid(accessToken)) {
        setStatus('authorized');
        return;
      }

      if (refreshToken) {
        try {
          const res = await fetch('/api/refresh_token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
          });

          if (res.ok) {
            const data = await res.json();
            sessionStorage.setItem('accessToken', data.accessToken);
            sessionStorage.setItem('refreshToken', data.refreshToken);
            setStatus('authorized');
            return;
          }
        } catch (err) {
          // fall through to unauthorized
        }
      }

      // If we get here, user is not authorized
      sessionStorage.clear();
      setStatus('unauthorized');
    };

    checkTokens();
  }, []);

  if (status === 'loading') {
    return <div className="card">Checking authorization...</div>;
  }

  if (status === 'unauthorized') {
    return (
      <div className="card" style={{ textAlign: 'center' }}>
        <h3>Please log in to see this message</h3>
        <p className="muted">You must be authenticated to view protected content.</p>
        <p>
          <a href="/login" className="button-ghost">Go to Login</a>
        </p>
      </div>
    );
  }

  return <>{children}</>;
}
