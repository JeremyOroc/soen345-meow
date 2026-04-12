import { useState } from 'react'
import './App.css'
import { AuthProvider, useAuth } from './auth/AuthContext'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import EventsPage from './pages/EventsPage'
import MyReservationsPage from './pages/MyReservationsPage'
import AdminDashboard from './pages/AdminDashboard'

function AppContent() {
  const { isAuthenticated, user, logout } = useAuth();
  const [currentPage, setCurrentPage] = useState<string>('events');

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
            <span style={{ marginLeft: 'auto', fontSize: '0.85em', color: '#9ca3af' }}>{user?.email}</span>
            <button onClick={logout}>Logout</button>
          </>
        ) : (
          <>
            <button onClick={() => setCurrentPage('login')} style={{ marginLeft: 'auto' }}>Log In</button>
            <button onClick={() => setCurrentPage('signup')}>Sign Up</button>
          </>
        )}
      </nav>

      {currentPage === 'events' && <EventsPage />}
      {currentPage === 'reservations' && isAuthenticated && <MyReservationsPage />}
      {currentPage === 'admin' && isAuthenticated && user?.role === 'ADMIN' && <AdminDashboard />}
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
