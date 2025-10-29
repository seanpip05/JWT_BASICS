import React from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import Login from './components/Login';
import TokenWrapper from './components/TokenWrapper';
import DataFetcher from './components/DataFetcher';

export default function App() {
  return (
    <div className="app-shell">
      <nav style={{ marginBottom: 12 }}>
        <Link to="/">Home</Link> | <Link to="/login">Login</Link> | <Link to="/data">Protected Data</Link>
      </nav>

      <Routes>
        <Route path="/" element={<div className="card"><h2>Welcome</h2><p>Use the links above to navigate.</p></div>} />
        <Route path="/login" element={<Login />} />
        <Route
          path="/data"
          element={
            <TokenWrapper>
              <DataFetcher />
            </TokenWrapper>
          }
        />
      </Routes>
    </div>
  );
}
