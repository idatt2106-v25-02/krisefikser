import posthog from 'posthog-js'
import { initializePostHogSdk, isPostHogSdkInitialized } from './posthog-sdk'

/** Bump when personverntekst eller cookie-praksis endres (revisjonsspor). */
export const PRIVACY_POLICY_VERSION = '2026-04-20'

const CONSENT_STORAGE_KEY = 'posthog_tracking_consent'
export const COOKIE_CONSENT_RECORD_KEY = 'krisefikser_cookie_consent_v1'
const LEGACY_COOKIES_ACCEPTED_KEY = 'cookiesAccepted'

export type ConsentSource =
  | 'banner_accept_all'
  | 'banner_necessary_only'
  | 'banner_reject'
  | 'settings'
  | 'legacy_migration'

export type CookieConsentRecord = {
  dismissed: boolean
  analytics: boolean
  policyVersion: string
  updatedAt: string
  source: ConsentSource
}

type TrackingConsent = 'granted' | 'denied' | null

let posthogActivationHandler: (() => void) | null = null

export function registerPostHogActivationHandler(handler: () => void): void {
  posthogActivationHandler = handler
}

function readLegacyPosthogConsent(): TrackingConsent {
  const storedConsent = localStorage.getItem(CONSENT_STORAGE_KEY)
  if (storedConsent === 'granted' || storedConsent === 'denied') {
    return storedConsent
  }
  return null
}

function writePosthogConsentFlag(consent: Exclude<TrackingConsent, null>): void {
  localStorage.setItem(CONSENT_STORAGE_KEY, consent)
}

function readConsentRecord(): CookieConsentRecord | null {
  try {
    const raw = localStorage.getItem(COOKIE_CONSENT_RECORD_KEY)
    if (!raw) {
      return null
    }
    const parsed = JSON.parse(raw) as CookieConsentRecord
    if (
      typeof parsed.dismissed === 'boolean' &&
      typeof parsed.analytics === 'boolean' &&
      typeof parsed.policyVersion === 'string' &&
      typeof parsed.updatedAt === 'string' &&
      typeof parsed.source === 'string'
    ) {
      return parsed
    }
  } catch {
    // ignore
  }
  return null
}

function writeConsentRecord(record: CookieConsentRecord): void {
  localStorage.setItem(COOKIE_CONSENT_RECORD_KEY, JSON.stringify(record))
}

/** Merge legacy keys into structured record (one-time migration on read). */
function migrateLegacyIfNeeded(): CookieConsentRecord | null {
  let record = readConsentRecord()
  if (record) {
    return record
  }

  const legacyCookies = localStorage.getItem(LEGACY_COOKIES_ACCEPTED_KEY)
  const legacyPh = readLegacyPosthogConsent()

  if (legacyCookies === null && legacyPh === null) {
    return null
  }

  const now = new Date().toISOString()

  if (legacyCookies === 'false') {
    record = {
      dismissed: true,
      analytics: false,
      policyVersion: PRIVACY_POLICY_VERSION,
      updatedAt: now,
      source: 'legacy_migration',
    }
  } else if (legacyCookies === 'true') {
    if (legacyPh === 'granted') {
      record = {
        dismissed: true,
        analytics: true,
        policyVersion: PRIVACY_POLICY_VERSION,
        updatedAt: now,
        source: 'legacy_migration',
      }
    } else if (legacyPh === 'denied') {
      record = {
        dismissed: true,
        analytics: false,
        policyVersion: PRIVACY_POLICY_VERSION,
        updatedAt: now,
        source: 'legacy_migration',
      }
    } else {
      // Tidligere: cookiesAccepted=true uten eksplisitt PostHog-flagg → behandle som samtykke til analyse
      record = {
        dismissed: true,
        analytics: true,
        policyVersion: PRIVACY_POLICY_VERSION,
        updatedAt: now,
        source: 'legacy_migration',
      }
    }
  } else if (legacyPh === 'granted' || legacyPh === 'denied') {
    record = {
      dismissed: true,
      analytics: legacyPh === 'granted',
      policyVersion: PRIVACY_POLICY_VERSION,
      updatedAt: now,
      source: 'legacy_migration',
    }
  }

  if (record) {
    writeConsentRecord(record)
    writePosthogConsentFlag(record.analytics ? 'granted' : 'denied')
  }

  return record
}

export function getConsentRecord(): CookieConsentRecord | null {
  return readConsentRecord() ?? migrateLegacyIfNeeded()
}

export function isCookieBannerDismissed(): boolean {
  const r = getConsentRecord()
  return r?.dismissed === true
}

export function hasAnalyticsConsent(): boolean {
  const r = getConsentRecord()
  return r?.analytics === true
}

function persistDecision(
  analytics: boolean,
  source: ConsentSource,
  legacyCookiesAccepted: 'true' | 'false',
): void {
  const now = new Date().toISOString()
  const record: CookieConsentRecord = {
    dismissed: true,
    analytics,
    policyVersion: PRIVACY_POLICY_VERSION,
    updatedAt: now,
    source,
  }
  writeConsentRecord(record)
  writePosthogConsentFlag(analytics ? 'granted' : 'denied')
  localStorage.setItem(LEGACY_COOKIES_ACCEPTED_KEY, legacyCookiesAccepted)
}

function activatePostHogAnalytics(): void {
  if (!initializePostHogSdk()) {
    return
  }
  posthog.opt_in_capturing()
  posthogActivationHandler?.()
}

function deactivatePostHogAnalytics(): void {
  if (isPostHogSdkInitialized()) {
    posthog.opt_out_capturing()
    posthog.reset()
  }
}

/**
 * Applies stored consent on app boot. PostHog SDK is only initialized when analytics is granted.
 */
export function applyStoredTrackingConsent(): void {
  const record = getConsentRecord()
  if (!record?.dismissed) {
    return
  }

  if (record.analytics) {
    activatePostHogAnalytics()
  } else {
    deactivatePostHogAnalytics()
  }
}

export function grantAllCookies(source: ConsentSource = 'banner_accept_all'): void {
  persistDecision(true, source, 'true')
  activatePostHogAnalytics()
}

export function grantNecessaryCookiesOnly(source: ConsentSource = 'banner_necessary_only'): void {
  persistDecision(false, source, 'true')
  deactivatePostHogAnalytics()
}

/** Full reject (banner «Avslå»): ingen valgfri analyse, samme som tidligere cookiesAccepted=false. */
export function rejectOptionalCookies(source: ConsentSource = 'banner_reject'): void {
  persistDecision(false, source, 'false')
  deactivatePostHogAnalytics()
}

/** Update analytics toggle from settings dialog. */
export function saveAnalyticsConsent(analytics: boolean, source: ConsentSource = 'settings'): void {
  persistDecision(analytics, source, 'true')
  if (analytics) {
    activatePostHogAnalytics()
  } else {
    deactivatePostHogAnalytics()
  }
}

/** @deprecated Use grantAllCookies / saveAnalyticsConsent */
export function grantTrackingConsent(): void {
  grantAllCookies('legacy_migration')
}

/** @deprecated Use rejectOptionalCookies / grantNecessaryCookiesOnly */
export function revokeTrackingConsent(): void {
  rejectOptionalCookies('legacy_migration')
}

export function hasTrackingConsent(): boolean {
  return hasAnalyticsConsent()
}
