import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function Login() {
  useEffect(() => {
    console.log('Rendering Login component');
  }, []);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const resp = await axios.post('/api/login', { username, password });
      sessionStorage.setItem('accessToken', resp.data.accessToken);
      sessionStorage.setItem('refreshToken', resp.data.refreshToken);
      navigate('/data');
    } catch (err) {
      setError('Login failed');
      console.error(err);
    }
  };

  return (
    <div style={{ maxWidth: 420, margin: '24px auto', padding: 12, border: '1px solid #ddd', borderRadius: 6 }}>
      <h2 style={{ marginTop: 0 }}>Login</h2>
      <form onSubmit={handleLogin}>
        <div style={{ marginBottom: 8 }}>
          <label style={{ display: 'block', fontSize: 12, marginBottom: 4 }}>Username</label>
          <input placeholder="username" value={username} onChange={e => setUsername(e.target.value)} style={{ width: '100%', padding: 8 }} />
        </div>
        <div style={{ marginBottom: 8 }}>
          <label style={{ display: 'block', fontSize: 12, marginBottom: 4 }}>Password</label>
          <input placeholder="password" type="password" value={password} onChange={e => setPassword(e.target.value)} style={{ width: '100%', padding: 8 }} />
        </div>
        <button type="submit" style={{ padding: '8px 12px' }}>Login</button>
        {error && <div style={{ color: 'red', marginTop: 8 }}>{error}</div>}
      </form>
    </div>
  );
}
