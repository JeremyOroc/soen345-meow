import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import MyReservationsPage from '../pages/MyReservationsPage';
import { AuthProvider } from '../auth/AuthContext';

const mockReservations = [
  { id: 1, eventId: 1, ticketQuantity: 2, status: 'CONFIRMED', reservedAt: '2026-04-10T10:00:00', cancelledAt: null },
  { id: 2, eventId: 2, ticketQuantity: 1, status: 'CANCELLED', reservedAt: '2026-04-09T08:00:00', cancelledAt: '2026-04-11T12:00:00' },
];

const mockEvents = [
  { id: 1, title: 'Rock Concert', eventDatetime: '2026-06-15T20:00:00', location: 'Montreal' },
  { id: 2, title: 'Basketball Game', eventDatetime: '2026-07-01T18:00:00', location: 'Toronto' },
];

function renderAuthenticated(ui: React.ReactElement) {
  const fakePayload = btoa(JSON.stringify({ sub: 'user@example.com', role: 'CUSTOMER' }));
  localStorage.setItem('token', `header.${fakePayload}.signature`);
  return render(<AuthProvider>{ui}</AuthProvider>);
}

describe('MyReservationsPage', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce({ ok: true, json: async () => mockReservations })
      .mockResolvedValueOnce({ ok: true, json: async () => mockEvents })
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('should render reservations list', async () => {
    renderAuthenticated(<MyReservationsPage />);

    await waitFor(() => {
      expect(screen.getByText('Rock Concert')).toBeInTheDocument();
      expect(screen.getByText('Basketball Game')).toBeInTheDocument();
    });

    expect(screen.getByText('CONFIRMED')).toBeInTheDocument();
    expect(screen.getByText('CANCELLED')).toBeInTheDocument();
  });

  it('should show cancel button on confirmed reservation', async () => {
    renderAuthenticated(<MyReservationsPage />);

    await waitFor(() => {
      expect(screen.getByText('Rock Concert')).toBeInTheDocument();
    });

    expect(screen.getByText('Cancel Reservation')).toBeInTheDocument();
  });
});
