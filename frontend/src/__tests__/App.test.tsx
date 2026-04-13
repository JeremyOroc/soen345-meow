import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import App from '../App';

describe('App Component', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('should render the Browse Events heading', () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [],
    }));

    render(<App />);
    const titleElement = screen.getByRole('heading', { name: /Browse Events/i });
    expect(titleElement).toBeInTheDocument();
  });

  it('should render the navigation bar', () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [],
    }));

    render(<App />);
    expect(screen.getByRole('button', { name: /Browse Events/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Log In/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Sign Up/i })).toBeInTheDocument();
  });
});
