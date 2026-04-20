import posthog from 'posthog-js'

const CONSENT_STORAGE_KEY = 'posthog_tracking_consent'

type TrackingConsent = 'granted' | 'denied' | null

function readConsent(): TrackingConsent {
  const storedConsent = localStorage.getItem(CONSENT_STORAGE_KEY)
  if (storedConsent === 'granted' || storedConsent === 'denied') {
    return storedConsent
  }
  return null
}

function writeConsent(consent: Exclude<TrackingConsent, null>) {
  localStorage.setItem(CONSENT_STORAGE_KEY, consent)
}

export function applyStoredTrackingConsent() {
  const consent = readConsent()

  if (consent === 'granted') {
    posthog.opt_in_capturing()
    return
  }

  // Default to denied until user has explicitly opted in.
  posthog.opt_out_capturing()
}

export function grantTrackingConsent() {
  writeConsent('granted')
  posthog.opt_in_capturing()
}

export function revokeTrackingConsent() {
  writeConsent('denied')
  posthog.opt_out_capturing()
}

export function hasTrackingConsent() {
  return readConsent() === 'granted'
}
