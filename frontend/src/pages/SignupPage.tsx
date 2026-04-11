import { useState } from 'react';

interface SignupPageProps {
  onNavigate: (page: string) => void;
}

export default function SignupPage({ onNavigate }: SignupPageProps) {
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    if (!email && !phone) {
      setError('Please provide at least an email or phone number.');
      return;
    }
    try {
      const res = await fetch('http://localhost:8080/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: email || null,
          phone: phone || null,
          password,
        }),
      });
      const data = await res.json();
      if (res.ok) {
        onNavigate('login');
      } else {
        setError(data.error || 'Signup failed. Please try again.');
      }
    } catch {
      setError('Something went wrong. Please try again.');
    }
  }

  return (
    <div style={{ maxWidth: '400px', margin: '60px auto', padding: '20px' }}>
      <h2>Sign Up</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        <input
          type="text"
          placeholder="Email"
          value={email}
          onChange={e => setEmail(e.target.value)}
        />
        <input
          type="text"
          placeholder="Phone"
          value={phone}
          onChange={e => setPhone(e.target.value)}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={e => setPassword(e.target.value)}
        />
        {error && <p style={{ color: '#ff6b6b', margin: 0 }}>{error}</p>}
        <button type="submit">Sign Up</button>
      </form>
      <p style={{ marginTop: '16px' }}>
        Already have an account?{' '}
        <button type="button" onClick={() => onNavigate('login')}>Log In</button>
      </p>
    </div>
  );
}
