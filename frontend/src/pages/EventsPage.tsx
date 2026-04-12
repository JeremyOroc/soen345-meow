import { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { apiUrl } from '../api';

interface EventItem {
  id: number;
  title: string;
  category: string;
  location: string;
  eventDatetime: string;
  availableSeats: number;
  capacity: number;
  status: string;
}

const CATEGORIES = ['All', 'Movies', 'Concerts', 'Travel', 'Sports'];

export default function EventsPage() {
  const { isAuthenticated, token } = useAuth();
  const [events, setEvents] = useState<EventItem[]>([]);
  const [loading, setLoading] = useState(true);

  const [categoryFilter, setCategoryFilter] = useState('All');
  const [locationFilter, setLocationFilter] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const [bookingEventId, setBookingEventId] = useState<number | null>(null);
  const [bookingQty, setBookingQty] = useState(1);
  const [bookingMessage, setBookingMessage] = useState<{ eventId: number; text: string; isError: boolean } | null>(null);
  const [bookingLoading, setBookingLoading] = useState(false);

  function buildUrl() {
    const params = new URLSearchParams();
    if (categoryFilter !== 'All') params.set('category', categoryFilter);
    if (locationFilter.trim()) params.set('location', locationFilter.trim());
    if (startDate) params.set('startDate', startDate + 'T00:00:00');
    if (endDate) params.set('endDate', endDate + 'T23:59:59');
    const qs = params.toString();
    return apiUrl('/api/events' + (qs ? `?${qs}` : ''));
  }

  function fetchEvents() {
    setLoading(true);
    fetch(buildUrl())
      .then(res => res.json())
      .then(data => {
        setEvents(data);
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }

  useEffect(() => {
    fetchEvents();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [categoryFilter, locationFilter, startDate, endDate]);

  function clearFilters() {
    setCategoryFilter('All');
    setLocationFilter('');
    setStartDate('');
    setEndDate('');
  }

  async function handleBook(eventId: number) {
    if (!token) return;
    setBookingLoading(true);
    setBookingMessage(null);
    try {
      const res = await fetch(apiUrl('/api/reservations'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ eventId, quantity: bookingQty }),
      });
      if (res.ok) {
        setBookingMessage({ eventId, text: 'Reservation confirmed! Check your email for details.', isError: false });
        setBookingEventId(null);
        setBookingQty(1);
        fetchEvents();
      } else {
        const data = await res.json();
        if (res.status === 409) {
          setBookingMessage({ eventId, text: data.error || 'No seats available.', isError: true });
        } else if (res.status === 401) {
          setBookingMessage({ eventId, text: 'You must be logged in to book tickets.', isError: true });
        } else {
          setBookingMessage({ eventId, text: data.error || 'Booking failed.', isError: true });
        }
      }
    } catch {
      setBookingMessage({ eventId, text: 'Something went wrong. Please try again.', isError: true });
    } finally {
      setBookingLoading(false);
    }
  }

  const hasActiveFilters = categoryFilter !== 'All' || locationFilter !== '' || startDate !== '' || endDate !== '';

  return (
    <div style={{ padding: '24px 32px' }}>
      <h2>Browse Events</h2>

      <div
        className="filter-bar"
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: '12px',
          alignItems: 'flex-end',
          marginBottom: '24px',
          padding: '16px',
          border: '1px solid var(--border)',
          borderRadius: '8px',
          background: 'var(--code-bg)',
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <label htmlFor="filter-location" style={{ fontSize: '0.8em', fontWeight: 600 }}>Location</label>
          <input
            id="filter-location"
            type="text"
            placeholder="Search location..."
            value={locationFilter}
            onChange={e => setLocationFilter(e.target.value)}
            style={{ padding: '6px 10px', borderRadius: '4px', border: '1px solid var(--border)', background: 'var(--bg)', color: 'var(--text-h)' }}
          />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <label htmlFor="filter-category" style={{ fontSize: '0.8em', fontWeight: 600 }}>Category</label>
          <select
            id="filter-category"
            value={categoryFilter}
            onChange={e => setCategoryFilter(e.target.value)}
            style={{ padding: '6px 10px', borderRadius: '4px', border: '1px solid var(--border)', background: 'var(--bg)', color: 'var(--text-h)' }}
          >
            {CATEGORIES.map(c => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <label htmlFor="filter-start-date" style={{ fontSize: '0.8em', fontWeight: 600 }}>Start Date</label>
          <input
            id="filter-start-date"
            type="date"
            value={startDate}
            onChange={e => setStartDate(e.target.value)}
            style={{ padding: '6px 10px', borderRadius: '4px', border: '1px solid var(--border)', background: 'var(--bg)', color: 'var(--text-h)' }}
          />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          <label htmlFor="filter-end-date" style={{ fontSize: '0.8em', fontWeight: 600 }}>End Date</label>
          <input
            id="filter-end-date"
            type="date"
            value={endDate}
            onChange={e => setEndDate(e.target.value)}
            style={{ padding: '6px 10px', borderRadius: '4px', border: '1px solid var(--border)', background: 'var(--bg)', color: 'var(--text-h)' }}
          />
        </div>

        {hasActiveFilters && (
          <button
            onClick={clearFilters}
            style={{ padding: '6px 14px', borderRadius: '4px', cursor: 'pointer' }}
          >
            Clear Filters
          </button>
        )}
      </div>

      {loading ? (
        <p>Loading events...</p>
      ) : events.length === 0 ? (
        <p>No events found matching your criteria.</p>
      ) : (
        <div className="event-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
          {events.map(event => (
            <div
              key={event.id}
              className="event-card"
              style={{
                border: '1px solid var(--border)',
                padding: '20px',
                borderRadius: '8px',
                background: 'var(--code-bg)',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
              }}
            >
              <h3 style={{ margin: 0 }}>{event.title}</h3>
              <p>📍 <strong>Location:</strong> {event.location}</p>
              <p>📅 <strong>Date:</strong> {event.eventDatetime}</p>
              <p>💺 <strong>Available Seats:</strong> {event.availableSeats ?? 'N/A'}</p>
              <span
                className="badge"
                style={{
                  background: '#646cff',
                  padding: '2px 8px',
                  borderRadius: '4px',
                  fontSize: '0.8em',
                  color: '#fff',
                  alignSelf: 'flex-start',
                }}
              >
                {event.category}
              </span>

              {isAuthenticated && event.availableSeats > 0 && (
                <div style={{ marginTop: '12px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {bookingEventId === event.id ? (
                    <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                      <label style={{ fontSize: '0.85em' }}>Qty:</label>
                      <input
                        type="number"
                        min={1}
                        max={event.availableSeats}
                        value={bookingQty}
                        onChange={e => setBookingQty(Math.max(1, Math.min(event.availableSeats, Number(e.target.value))))}
                        style={{ width: '60px', padding: '4px 6px', borderRadius: '4px', border: '1px solid var(--border)' }}
                      />
                      <button
                        onClick={() => handleBook(event.id)}
                        disabled={bookingLoading}
                        style={{ padding: '4px 12px', borderRadius: '4px', cursor: 'pointer' }}
                      >
                        {bookingLoading ? 'Booking...' : 'Confirm'}
                      </button>
                      <button
                        onClick={() => { setBookingEventId(null); setBookingQty(1); }}
                        style={{ padding: '4px 12px', borderRadius: '4px', cursor: 'pointer' }}
                      >
                        Cancel
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => { setBookingEventId(event.id); setBookingQty(1); setBookingMessage(null); }}
                      style={{ padding: '8px 16px', borderRadius: '4px', cursor: 'pointer', alignSelf: 'flex-start' }}
                    >
                      Book Ticket
                    </button>
                  )}
                </div>
              )}

              {bookingMessage && bookingMessage.eventId === event.id && (
                <p style={{ color: bookingMessage.isError ? '#ff6b6b' : '#4ade80', fontSize: '0.9em', margin: 0 }}>
                  {bookingMessage.text}
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
