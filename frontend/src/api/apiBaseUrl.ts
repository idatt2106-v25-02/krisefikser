/**
 * Base URL for the Spring API (no trailing slash).
 * Production builds must set VITE_API_URL at build time (e.g. Railway / Docker ARG).
 */
export function getApiBaseUrl(): string {
  const raw = import.meta.env.VITE_API_URL
  const trimmed = typeof raw === 'string' ? raw.trim() : ''
  if (trimmed) {
    return trimmed.replace(/\/+$/, '')
  }
  if (import.meta.env.DEV) {
    return 'http://localhost:8080'
  }
  return ''
}
