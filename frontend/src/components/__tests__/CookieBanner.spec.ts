import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import CookieBanner from '@/components/CookieBanner.vue'
import {
  grantTrackingConsent,
  hasTrackingConsent,
  revokeTrackingConsent,
} from '@/plugins/docs/posthog-consent'

vi.mock('@/plugins/docs/posthog-consent', () => ({
  grantTrackingConsent: vi.fn(),
  revokeTrackingConsent: vi.fn(),
  hasTrackingConsent: vi.fn(() => false),
}))

describe('CookieBanner', () => {
  let localStorageMock: { [key: string]: string }

  beforeEach(() => {
    // Reset localStorage mock before each test
    localStorageMock = {}

    // Mock localStorage
    vi.stubGlobal('localStorage', {
      getItem: vi.fn((key: string) => localStorageMock[key]),
      setItem: vi.fn((key: string, value: string) => {
        localStorageMock[key] = value
      })
    })
  })

  it('renders when cookies have not been accepted', () => {
    const wrapper = mount(CookieBanner)
    expect(wrapper.isVisible()).toBe(true)
    expect(wrapper.text()).toContain('Vi bruker informasjonskapsler')
  })

  it('does not render when cookies have been previously accepted', () => {
    localStorageMock['cookiesAccepted'] = 'true'
    const wrapper = mount(CookieBanner)
    expect(wrapper.isVisible()).toBe(false)
  })

  it('hides banner and sets localStorage when accepting cookies', async () => {
    const wrapper = mount(CookieBanner)

    // Click accept button
    await wrapper.findAll('button')[0].trigger('click')

    // Check if banner is hidden
    expect(wrapper.isVisible()).toBe(false)

    // Check if localStorage was updated
    expect(localStorage.setItem).toHaveBeenCalledWith('cookiesAccepted', 'true')
    expect(grantTrackingConsent).toHaveBeenCalled()
  })

  it('hides banner and revokes tracking when rejecting cookies', async () => {
    const wrapper = mount(CookieBanner)

    // Click reject button
    await wrapper.findAll('button')[1].trigger('click')

    // Check if banner is hidden
    expect(wrapper.isVisible()).toBe(false)

    // Check if localStorage was updated
    expect(localStorage.setItem).toHaveBeenCalledWith('cookiesAccepted', 'false')
    expect(revokeTrackingConsent).toHaveBeenCalled()
  })

  it('has correct styling classes', () => {
    const wrapper = mount(CookieBanner)

    // Check container classes
    const container = wrapper.find('.container')
    expect(container.classes()).toContain('mx-auto')
    expect(container.classes()).toContain('flex')

    // Check button classes
    const button = wrapper.find('button')
    expect(button.classes()).toContain('bg-blue-500')
    expect(button.classes()).toContain('hover:bg-blue-600')
    expect(button.classes()).toContain('text-white')
  })

  it('checks cookie consent on mount', () => {
    mount(CookieBanner)
    expect(localStorage.getItem).toHaveBeenCalledWith('cookiesAccepted')
  })

  it('migrates existing accepted cookies to posthog consent', () => {
    localStorageMock['cookiesAccepted'] = 'true'
    vi.mocked(hasTrackingConsent).mockReturnValue(false)

    const wrapper = mount(CookieBanner)

    expect(wrapper.isVisible()).toBe(false)
    expect(grantTrackingConsent).toHaveBeenCalled()
  })

  it('keeps tracking disabled when cookies were previously rejected', () => {
    localStorageMock['cookiesAccepted'] = 'false'

    const wrapper = mount(CookieBanner)

    expect(wrapper.isVisible()).toBe(false)
    expect(revokeTrackingConsent).toHaveBeenCalled()
  })
})
