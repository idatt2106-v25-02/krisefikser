import posthog from 'posthog-js'

let sdkInitialized = false

/**
 * Initializes PostHog exactly once, after the user has granted analytics consent.
 * Must not be called before consent.
 */
export function initializePostHogSdk(): boolean {
  if (sdkInitialized) {
    return true
  }

  const key = import.meta.env.VITE_POSTHOG_KEY
  const host = import.meta.env.VITE_POSTHOG_HOST || 'https://eu.i.posthog.com'

  if (!key) {
    console.warn('PostHog key is missing. Skipping analytics initialization.')
    return false
  }

  posthog.init(key, {
    api_host: host,
    capture_pageview: false,
    autocapture: false,
    persistence: 'localStorage',
  })

  sdkInitialized = true
  return true
}

export function isPostHogSdkInitialized(): boolean {
  return sdkInitialized
}
