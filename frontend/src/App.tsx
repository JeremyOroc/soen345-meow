import { useState, useEffect } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'
import { AuthProvider, useAuth } from './auth/AuthContext'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import { apiUrl } from './api'

interface EventItem {
  id: number;
  title: string;
  category: string;
  location: string;
  eventDatetime: string;
}

function AppContent() {
  const { isAuthenticated, user, logout } = useAuth();
  const [currentPage, setCurrentPage] = useState<string>('events');
  const [events, setEvents] = useState<EventItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(apiUrl('/api/events'))
      .then(res => res.json())
      .then(data => {
        setEvents(data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Failed to fetch events:", err);
        setLoading(false);
      });
  }, []);

  if (currentPage === 'login') {
    return <LoginPage onNavigate={(page) => setCurrentPage(page)} />;
  }

  if (currentPage === 'signup') {
    return <SignupPage onNavigate={(page) => setCurrentPage(page)} />;
  }

  return (
    <>
      <nav style={{ padding: '10px 20px', background: '#1a1a1a', display: 'flex', gap: '16px', alignItems: 'center' }}>
        <button onClick={() => setCurrentPage('events')}>Browse Events</button>
        {isAuthenticated ? (
          <>
            <button onClick={() => setCurrentPage('reservations')}>My Reservations</button>
            {user?.role === 'ADMIN' && (
              <button onClick={() => setCurrentPage('admin')}>Admin Dashboard</button>
            )}
            <span style={{ marginLeft: 'auto', fontSize: '0.85em' }}>{user?.email}</span>
            <button onClick={logout}>Logout</button>
          </>
        ) : (
          <>
            <button onClick={() => setCurrentPage('login')}>Log In</button>
            <button onClick={() => setCurrentPage('signup')}>Sign Up</button>
          </>
        )}
      </nav>

      <section id="center">
        <div className="hero">
          <img src={heroImg} className="base" width="170" height="179" alt="" />
          <img src={reactLogo} className="framework" alt="React logo" />
          <img src={viteLogo} className="vite" alt="Vite logo" />
        </div>
        <div>
          <h1>Browse Events</h1>
          <p>Active events available for booking</p>
        </div>
      </section>

      <div className="ticks"></div>

      <section id="next-steps">
        <div id="docs" style={{ width: '100%' }}>
          <h2>Available Events</h2>
          {loading ? (
            <p>Loading events from database...</p>
          ) : events.length === 0 ? (
            <p>No active events found. Add some to your <code>events</code> table!</p>
          ) : (
            <div className="event-grid" style={{ display: 'grid', gap: '20px', textAlign: 'left' }}>
              {events.map(event => (
                <div key={event.id} className="event-card" style={{ border: '1px solid #444', padding: '15px', borderRadius: '8px' }}>
                  <h3>{event.title}</h3>
                  <p>📍 <strong>Location:</strong> {event.location}</p>
                  <p>📅 <strong>Date:</strong> {event.eventDatetime}</p>
                  <span className="badge" style={{ background: '#646cff', padding: '2px 8px', borderRadius: '4px', fontSize: '0.8em' }}>
                    {event.category}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      <div className="ticks"></div>
      <section id="spacer"></section>
    </>
  )
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App
