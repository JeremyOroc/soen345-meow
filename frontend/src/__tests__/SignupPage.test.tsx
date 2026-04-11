import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import SignupPage from '../pages/SignupPage';
import { AuthProvider } from '../auth/AuthContext';

describe('SignupPage', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('should render signup form', () => {
    render(
      <AuthProvider>
        <SignupPage onNavigate={() => {}} />
      </AuthProvider>
    );

    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/phone/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign up/i })).toBeInTheDocument();
  });

  it('should show error on duplicate email', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 409,
      json: async () => ({ error: 'Email already in use' }),
    }));

    render(
      <AuthProvider>
        <SignupPage onNavigate={() => {}} />
      </AuthProvider>
    );

    fireEvent.change(screen.getByPlaceholderText(/email/i), {
      target: { value: 'existing@example.com' },
    });
    fireEvent.change(screen.getByPlaceholderText(/password/i), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(screen.getByText(/already in use/i)).toBeInTheDocument();
    });
  });

  it('should succeed on valid signup', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      json: async () => ({ message: 'User registered successfully' }),
    }));

    const onNavigate = vi.fn();

    render(
      <AuthProvider>
        <SignupPage onNavigate={onNavigate} />
      </AuthProvider>
    );

    fireEvent.change(screen.getByPlaceholderText(/email/i), {
      target: { value: 'new@example.com' },
    });
    fireEvent.change(screen.getByPlaceholderText(/password/i), {
      target: { value: 'password123' },
    });
    fireEvent.click(screen.getByRole('button', { name: /sign up/i }));

    await waitFor(() => {
      expect(onNavigate).toHaveBeenCalledWith('login');
    });
  });
});
