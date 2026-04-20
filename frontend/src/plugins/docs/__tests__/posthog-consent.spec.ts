import { describe, it, expect, beforeEach, vi } from 'vitest'
import posthog from 'posthog-js'
import { initializePostHogSdk, isPostHogSdkInitialized } from '../posthog-sdk'

vi.mock('posthog-js', () => ({
  default: {
    init: vi.fn(),
    opt_in_capturing: vi.fn(),
    opt_out_capturing: vi.fn(),
    reset: vi.fn(),
  },
}))

vi.mock('../posthog-sdk', () => ({
  initializePostHogSdk: vi.fn(() => true),
  isPostHogSdkInitialized: vi.fn(() => false),
}))

import {
  applyStoredTrackingConsent,
  grantAllCookies,
  grantNecessaryCookiesOnly,
  COOKIE_CONSENT_RECORD_KEY,
  PRIVACY_POLICY_VERSION,
  registerPostHogActivationHandler,
} from '../posthog-consent'

describe('posthog-consent', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('does not initialize PostHog when analytics consent is denied', () => {
    vi.mocked(isPostHogSdkInitialized).mockReturnValue(false)
    localStorage.setItem(
      COOKIE_CONSENT_RECORD_KEY,
      JSON.stringify({
        dismissed: true,
        analytics: false,
        policyVersion: PRIVACY_POLICY_VERSION,
        updatedAt: new Date().toISOString(),
        source: 'banner_necessary_only',
      }),
    )
    localStorage.setItem('posthog_tracking_consent', 'denied')

    const handler = vi.fn()
    registerPostHogActivationHandler(handler)

    applyStoredTrackingConsent()

    expect(posthog.init).not.toHaveBeenCalled()
    expect(handler).not.toHaveBeenCalled()
    expect(posthog.opt_out_capturing).not.toHaveBeenCalled()
  })

  it('calls activation handler when analytics consent is granted', () => {
    vi.mocked(isPostHogSdkInitialized).mockReturnValue(true)
    localStorage.setItem(
      COOKIE_CONSENT_RECORD_KEY,
      JSON.stringify({
        dismissed: true,
        analytics: true,
        policyVersion: PRIVACY_POLICY_VERSION,
        updatedAt: new Date().toISOString(),
        source: 'banner_accept_all',
      }),
    )
    localStorage.setItem('posthog_tracking_consent', 'granted')

    const handler = vi.fn()
    registerPostHogActivationHandler(handler)

    applyStoredTrackingConsent()

    expect(initializePostHogSdk).toHaveBeenCalled()
    expect(posthog.opt_in_capturing).toHaveBeenCalled()
    expect(handler).toHaveBeenCalled()
  })

  it('persists audit fields on grantAllCookies', () => {
    const handler = vi.fn()
    registerPostHogActivationHandler(handler)
    grantAllCookies('banner_accept_all')

    const raw = localStorage.getItem(COOKIE_CONSENT_RECORD_KEY)
    expect(raw).toBeTruthy()
    const record = JSON.parse(raw!) as {
      analytics: boolean
      policyVersion: string
      updatedAt: string
      source: string
    }
    expect(record.analytics).toBe(true)
    expect(record.policyVersion).toBe(PRIVACY_POLICY_VERSION)
    expect(record.updatedAt).toMatch(/^\d{4}-/)
    expect(record.source).toBe('banner_accept_all')
  })

  it('persists denied analytics on grantNecessaryCookiesOnly', () => {
    registerPostHogActivationHandler(vi.fn())
    grantNecessaryCookiesOnly('banner_necessary_only')

    const record = JSON.parse(localStorage.getItem(COOKIE_CONSENT_RECORD_KEY)!)
    expect(record.analytics).toBe(false)
    expect(localStorage.getItem('posthog_tracking_consent')).toBe('denied')
  })
})
