/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_WS_URL: string
  /** Cloudflare Turnstile site key (Docker bruker test-nøkkel som standard via compose). */
  readonly VITE_TURNSTILE_SITE_KEY?: string
  readonly VITE_POSTHOG_ENABLED: string
  readonly VITE_POSTHOG_KEY: string
  readonly VITE_POSTHOG_HOST?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
