const DEFAULT_API_BASE_URL = 'http://localhost:8080';

export const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL?.toString().trim() || DEFAULT_API_BASE_URL
).replace(/\/$/, '');

export function apiUrl(path: string): string {
  if (!path.startsWith('/')) {
    return `${API_BASE_URL}/${path}`;
  }
  return `${API_BASE_URL}${path}`;
}
