import React, { useEffect, useState } from 'react';
import axios from 'axios';

export default function DataFetcher() {
  const [data, setData] = useState(null);
  const [err, setErr] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = sessionStorage.getItem('accessToken');
        const resp = await axios.get('/api/protected-message', {
          headers: { Authorization: `Bearer ${token}` }
        });
        setData(resp.data);
      } catch (e) {
        setErr('Failed to fetch protected data');
        console.error(e);
      }
    };
    fetchData();
  }, []);

  if (err) return <div>{err}</div>;
  if (!data) return <div>Loading...</div>;
  return <pre>{JSON.stringify(data, null, 2)}</pre>;
}

