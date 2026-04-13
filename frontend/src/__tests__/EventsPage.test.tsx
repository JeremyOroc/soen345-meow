import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import EventsPage from '../pages/EventsPage';
import { AuthProvider } from '../auth/AuthContext';

const mockEvents = [
  { id: 1, title: 'Rock Concert', category: 'Concerts', location: 'Montreal', eventDatetime: '2026-06-15T20:00:00', availableSeats: 50, capacity: 100, status: 'ACTIVE' },
  { id: 2, title: 'Basketball Game', category: 'Sports', location: 'Toronto', eventDatetime: '2026-07-01T18:00:00', availableSeats: 200, capacity: 500, status: 'ACTIVE' },
];

function renderWithAuth(ui: React.ReactElement, authenticated = false) {
  if (authenticated) {
    const fakePayload = btoa(JSON.stringify({ sub: 'user@example.com', role: 'CUSTOMER' }));
    localStorage.setItem('token', `header.${fakePayload}.signature`);
  } else {
    localStorage.removeItem('token');
  }
  return render(<AuthProvider>{ui}</AuthProvider>);
}

describe('EventsPage', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockEvents,
    }));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('should render filter bar', async () => {
    renderWithAuth(<EventsPage />);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search location/i)).toBeInTheDocument();
    });
    expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/start date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/end date/i)).toBeInTheDocument();
  });

  it('should filter events by category', async () => {
    const filteredEvents = [mockEvents[0]];
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: async () => mockEvents })
      .mockResolvedValueOnce({ ok: true, json: async () => filteredEvents });

    vi.stubGlobal('fetch', fetchMock);

    renderWithAuth(<EventsPage />);

    await waitFor(() => {
      expect(screen.getByText('Rock Concert')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/category/i), { target: { value: 'Concerts' } });

    await waitFor(() => {
      const lastCall = fetchMock.mock.calls[fetchMock.mock.calls.length - 1][0] as string;
      expect(lastCall).toContain('category=Concerts');
    });
  });

  it('should show book button when authenticated', async () => {
    renderWithAuth(<EventsPage />, true);

    await waitFor(() => {
      expect(screen.getByText('Rock Concert')).toBeInTheDocument();
    });

    const bookButtons = screen.getAllByText('Book Ticket');
    expect(bookButtons.length).toBeGreaterThan(0);
  });

  it('should hide book button when not authenticated', async () => {
    renderWithAuth(<EventsPage />, false);

    await waitFor(() => {
      expect(screen.getByText('Rock Concert')).toBeInTheDocument();
    });

    expect(screen.queryByText('Book Ticket')).not.toBeInTheDocument();
  });
});
