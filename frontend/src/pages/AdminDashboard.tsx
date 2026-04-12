import { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { apiUrl } from '../api';

const CATEGORIES = ['Movies', 'Concerts', 'Travel', 'Sports'];

interface EventItem {
  id: number;
  title: string;
  category: string;
  location: string;
  eventDatetime: string;
  capacity: number;
  availableSeats: number;
  status: string;
}

interface EventForm {
  title: string;
  category: string;
  location: string;
  eventDatetime: string;
  capacity: string;
}

const EMPTY_FORM: EventForm = { title: '', category: '', location: '', eventDatetime: '', capacity: '' };

export default function AdminDashboard() {
  const { token } = useAuth();
  const [events, setEvents] = useState<EventItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState<EventForm>(EMPTY_FORM);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [confirmCancelId, setConfirmCancelId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  };

  async function fetchAllEvents() {
    setLoading(true);
    try {
      const res = await fetch(apiUrl('/api/events'));
      if (res.ok) {
        setEvents(await res.json());
      }
    } catch {
      setError('Failed to load events.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchAllEvents();
  }, []);

  function updateField(field: keyof EventForm, value: string) {
    setForm(prev => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setMessage('');

    const body = {
      title: form.title,
      category: form.category,
      location: form.location,
      eventDatetime: form.eventDatetime,
      capacity: Number(form.capacity),
    };

    try {
      let res: Response;
      if (editingId !== null) {
        res = await fetch(apiUrl(`/api/admin/events/${editingId}`), {
          method: 'PUT',
          headers,
          body: JSON.stringify(body),
        });
      } else {
        res = await fetch(apiUrl('/api/admin/events'), {
          method: 'POST',
          headers,
          body: JSON.stringify(body),
        });
      }

      if (res.ok) {
        setMessage(editingId !== null ? 'Event updated successfully.' : 'Event created successfully.');
        setForm(EMPTY_FORM);
        setEditingId(null);
        fetchAllEvents();
      } else {
        const data = await res.json();
        setError(data.error || 'Operation failed.');
      }
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  function startEdit(event: EventItem) {
    setEditingId(event.id);
    setForm({
      title: event.title,
      category: event.category,
      location: event.location,
      eventDatetime: event.eventDatetime,
      capacity: String(event.capacity),
    });
    setMessage('');
    setError('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  async function handleCancelEvent(eventId: number) {
    setError('');
    setMessage('');
    try {
      const res = await fetch(apiUrl(`/api/admin/events/${eventId}/cancel`), {
        method: 'DELETE',
        headers,
      });
      if (res.ok) {
        setMessage('Event cancelled successfully.');
        setConfirmCancelId(null);
        fetchAllEvents();
      } else {
        const data = await res.json();
        setError(data.error || 'Failed to cancel event.');
      }
    } catch {
      setError('Something went wrong. Please try again.');
    }
  }

  const inputStyle = {
    padding: '8px 10px',
    borderRadius: '4px',
    border: '1px solid var(--border)',
    background: 'var(--bg)',
    color: 'var(--text-h)',
    width: '100%',
    boxSizing: 'border-box' as const,
  };

  return (
    <div style={{ padding: '24px 32px' }}>
      <h2>Admin Dashboard</h2>

      {message && <p style={{ color: '#4ade80' }}>{message}</p>}
      {error && <p style={{ color: '#ff6b6b' }}>{error}</p>}

      <div style={{ border: '1px solid var(--border)', borderRadius: '8px', padding: '20px', marginBottom: '32px', background: 'var(--code-bg)' }}>
        <h3 style={{ marginTop: 0 }}>{editingId !== null ? 'Edit Event' : 'Add Event'}</h3>
        <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <label style={{ fontSize: '0.8em', fontWeight: 600 }}>Title</label>
            <input
              type="text"
              placeholder="Event title"
              value={form.title}
              onChange={e => updateField('title', e.target.value)}
              required
              style={inputStyle}
            />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <label style={{ fontSize: '0.8em', fontWeight: 600 }}>Category</label>
            <select
              value={form.category}
              onChange={e => updateField('category', e.target.value)}
              required
              style={inputStyle}
            >
              <option value="">Select category</option>
              {CATEGORIES.map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <label style={{ fontSize: '0.8em', fontWeight: 600 }}>Location</label>
            <input
              type="text"
              placeholder="Event location"
              value={form.location}
              onChange={e => updateField('location', e.target.value)}
              required
              style={inputStyle}
            />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <label style={{ fontSize: '0.8em', fontWeight: 600 }}>Date & Time</label>
            <input
              type="datetime-local"
              value={form.eventDatetime}
              onChange={e => updateField('eventDatetime', e.target.value)}
              required
              style={inputStyle}
            />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <label style={{ fontSize: '0.8em', fontWeight: 600 }}>Capacity</label>
            <input
              type="number"
              min={1}
              placeholder="Total seats"
              value={form.capacity}
              onChange={e => updateField('capacity', e.target.value)}
              required
              style={inputStyle}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px' }}>
            <button type="submit" disabled={submitting} style={{ padding: '8px 20px', borderRadius: '4px', cursor: 'pointer' }}>
              {submitting ? 'Saving...' : editingId !== null ? 'Update Event' : 'Create Event'}
            </button>
            {editingId !== null && (
              <button type="button" onClick={cancelEdit} style={{ padding: '8px 20px', borderRadius: '4px', cursor: 'pointer' }}>
                Cancel Edit
              </button>
            )}
          </div>
        </form>
      </div>

      <h3>All Events</h3>
      {loading ? (
        <p>Loading events...</p>
      ) : events.length === 0 ? (
        <p>No events found.</p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9em' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid var(--border)', textAlign: 'left' }}>
                <th style={{ padding: '8px' }}>Title</th>
                <th style={{ padding: '8px' }}>Category</th>
                <th style={{ padding: '8px' }}>Location</th>
                <th style={{ padding: '8px' }}>Date</th>
                <th style={{ padding: '8px' }}>Capacity</th>
                <th style={{ padding: '8px' }}>Available</th>
                <th style={{ padding: '8px' }}>Status</th>
                <th style={{ padding: '8px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {events.map(event => (
                <tr key={event.id} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '8px' }}>{event.title}</td>
                  <td style={{ padding: '8px' }}>{event.category}</td>
                  <td style={{ padding: '8px' }}>{event.location}</td>
                  <td style={{ padding: '8px' }}>{event.eventDatetime}</td>
                  <td style={{ padding: '8px' }}>{event.capacity}</td>
                  <td style={{ padding: '8px' }}>{event.availableSeats}</td>
                  <td style={{ padding: '8px' }}>
                    <span style={{
                      padding: '2px 8px',
                      borderRadius: '4px',
                      fontSize: '0.85em',
                      fontWeight: 600,
                      color: '#fff',
                      background: event.status === 'ACTIVE' ? '#22c55e' : '#ef4444',
                    }}>
                      {event.status}
                    </span>
                  </td>
                  <td style={{ padding: '8px' }}>
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <button onClick={() => startEdit(event)} style={{ padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85em' }}>
                        Edit
                      </button>
                      {event.status === 'ACTIVE' && (
                        confirmCancelId === event.id ? (
                          <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
                            <button
                              onClick={() => handleCancelEvent(event.id)}
                              style={{ padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85em', background: '#ef4444', color: '#fff', border: 'none' }}
                            >
                              Confirm
                            </button>
                            <button
                              onClick={() => setConfirmCancelId(null)}
                              style={{ padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85em' }}
                            >
                              No
                            </button>
                          </div>
                        ) : (
                          <button
                            onClick={() => setConfirmCancelId(event.id)}
                            style={{ padding: '4px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85em' }}
                          >
                            Cancel Event
                          </button>
                        )
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
