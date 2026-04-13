import { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { apiUrl } from '../api';

interface ReservationItem {
  id: number;
  eventId: number;
  ticketQuantity: number;
  status: string;
  reservedAt: string;
  cancelledAt: string | null;
}

interface EventItem {
  id: number;
  title: string;
  eventDatetime: string;
  location: string;
}

export default function MyReservationsPage() {
  const { token } = useAuth();
  const [reservations, setReservations] = useState<ReservationItem[]>([]);
  const [events, setEvents] = useState<Record<number, EventItem>>({});
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [confirmCancelId, setConfirmCancelId] = useState<number | null>(null);
  const [error, setError] = useState('');

  async function fetchReservations() {
    setLoading(true);
    try {
      const res = await fetch(apiUrl('/api/reservations/my'), {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data: ReservationItem[] = await res.json();
        setReservations(data);

        const eventsRes = await fetch(apiUrl('/api/events'));
        if (eventsRes.ok) {
          const allEvents: EventItem[] = await eventsRes.json();
          const map: Record<number, EventItem> = {};
          allEvents.forEach(e => { map[e.id] = e; });
          setEvents(map);
        }
      }
    } catch {
      setError('Failed to load reservations.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchReservations();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleCancel(reservationId: number) {
    setCancellingId(reservationId);
    setError('');
    try {
      const res = await fetch(apiUrl(`/api/reservations/${reservationId}`), {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        setConfirmCancelId(null);
        fetchReservations();
      } else {
        const data = await res.json();
        setError(data.error || 'Failed to cancel reservation.');
      }
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setCancellingId(null);
    }
  }

  if (loading) {
    return <div style={{ padding: '24px 32px' }}><p>Loading reservations...</p></div>;
  }

  return (
    <div style={{ padding: '24px 32px' }}>
      <h2>My Reservations</h2>
      {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

      {reservations.length === 0 ? (
        <p>You have no reservations yet.</p>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
          {reservations.map(r => {
            const event = events[r.eventId];
            return (
              <div
                key={r.id}
                style={{
                  border: '1px solid var(--border)',
                  padding: '16px',
                  borderRadius: '8px',
                  background: 'var(--code-bg)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '6px',
                }}
              >
                <h3 style={{ margin: 0 }}>{event?.title || `Event #${r.eventId}`}</h3>
                {event && <p>📅 {event.eventDatetime}</p>}
                {event && <p>📍 {event.location}</p>}
                <p>🎟️ <strong>Tickets:</strong> {r.ticketQuantity}</p>
                <p>📆 <strong>Booked:</strong> {new Date(r.reservedAt).toLocaleString()}</p>
                <span
                  style={{
                    display: 'inline-block',
                    padding: '2px 10px',
                    borderRadius: '4px',
                    fontSize: '0.8em',
                    fontWeight: 600,
                    color: '#fff',
                    alignSelf: 'flex-start',
                    background: r.status === 'CONFIRMED' ? '#22c55e' : '#ef4444',
                  }}
                >
                  {r.status}
                </span>

                {r.status === 'CONFIRMED' && (
                  <div style={{ marginTop: '8px' }}>
                    {confirmCancelId === r.id ? (
                      <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                        <span style={{ fontSize: '0.85em' }}>Are you sure?</span>
                        <button
                          onClick={() => handleCancel(r.id)}
                          disabled={cancellingId === r.id}
                          style={{ padding: '4px 12px', borderRadius: '4px', cursor: 'pointer', background: '#ef4444', color: '#fff', border: 'none' }}
                        >
                          {cancellingId === r.id ? 'Cancelling...' : 'Yes, Cancel'}
                        </button>
                        <button
                          onClick={() => setConfirmCancelId(null)}
                          style={{ padding: '4px 12px', borderRadius: '4px', cursor: 'pointer' }}
                        >
                          No
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => setConfirmCancelId(r.id)}
                        style={{ padding: '6px 14px', borderRadius: '4px', cursor: 'pointer' }}
                      >
                        Cancel Reservation
                      </button>
                    )}
                  </div>
                )}

                {r.status === 'CANCELLED' && r.cancelledAt && (
                  <p style={{ fontSize: '0.85em', color: 'var(--text)' }}>
                    Cancelled: {new Date(r.cancelledAt).toLocaleString()}
                  </p>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
