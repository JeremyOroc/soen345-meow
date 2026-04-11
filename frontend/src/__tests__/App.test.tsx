import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from '../App';

describe('App Component', () => {
  it('should render the Browse Events title', () => {
    render(<App />);
    const titleElement = screen.getByRole('heading', { name: /Browse Events/i, level: 1 });
    expect(titleElement).toBeInTheDocument();
  });

  it('should display the loading state initially', () => {
    render(<App />);
    const loadingMessage = screen.getByText(/Loading events from database/i);
    expect(loadingMessage).toBeInTheDocument();
  });
});
