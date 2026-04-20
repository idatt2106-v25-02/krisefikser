import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import CookieBanner from '@/components/CookieBanner.vue'
import {
  grantAllCookies,
  grantNecessaryCookiesOnly,
  rejectOptionalCookies,
  isCookieBannerDismissed,
} from '@/plugins/docs/posthog-consent'
import { openCookieSettings } from '@/plugins/docs/cookie-settings-ui'

vi.mock('@/plugins/docs/posthog-consent', () => ({
  grantAllCookies: vi.fn(),
  grantNecessaryCookiesOnly: vi.fn(),
  rejectOptionalCookies: vi.fn(),
  isCookieBannerDismissed: vi.fn(() => false),
}))

vi.mock('@/plugins/docs/cookie-settings-ui', () => ({
  openCookieSettings: vi.fn(),
}))

describe('CookieBanner', () => {
  let localStorageMock: { [key: string]: string }

  const mountBanner = () =>
    mount(CookieBanner, {
      global: {
        stubs: { RouterLink: { template: '<a><slot /></a>' } },
      },
    })

  beforeEach(() => {
    localStorageMock = {}
    vi.mocked(isCookieBannerDismissed).mockReturnValue(false)

    vi.stubGlobal('localStorage', {
      getItem: vi.fn((key: string) => localStorageMock[key]),
      setItem: vi.fn((key: string, value: string) => {
        localStorageMock[key] = value
      }),
      removeItem: vi.fn((key: string) => {
        delete localStorageMock[key]
      }),
      clear: vi.fn(() => {
        localStorageMock = {}
      }),
    })
  })

  it('renders when cookies have not been accepted', () => {
    const wrapper = mountBanner()
    expect(wrapper.isVisible()).toBe(true)
    expect(wrapper.text()).toContain('Vi bruker nødvendige informasjonskapsler')
  })

  it('does not render when consent was previously recorded', () => {
    vi.mocked(isCookieBannerDismissed).mockReturnValue(true)
    const wrapper = mountBanner()
    expect(wrapper.find('[role="region"]').exists()).toBe(false)
  })

  it('hides banner and calls grantAllCookies when accepting all', async () => {
    const wrapper = mountBanner()
    await wrapper.get('button').trigger('click')
    expect(wrapper.find('[role="region"]').exists()).toBe(false)
    expect(grantAllCookies).toHaveBeenCalledWith('banner_accept_all')
  })

  it('hides banner and calls grantNecessaryCookiesOnly for necessary-only', async () => {
    const wrapper = mountBanner()
    const buttons = wrapper.findAll('button')
    await buttons[1].trigger('click')
    expect(grantNecessaryCookiesOnly).toHaveBeenCalledWith('banner_necessary_only')
  })

  it('hides banner and calls rejectOptionalCookies for reject optional', async () => {
    const wrapper = mountBanner()
    const buttons = wrapper.findAll('button')
    await buttons[2].trigger('click')
    expect(rejectOptionalCookies).toHaveBeenCalledWith('banner_reject')
  })

  it('opens cookie settings when clicking Tilpass', async () => {
    const wrapper = mountBanner()
    const buttons = wrapper.findAll('button')
    await buttons[3].trigger('click')
    expect(openCookieSettings).toHaveBeenCalled()
  })

  it('checks cookie consent on mount', () => {
    mountBanner()
    expect(isCookieBannerDismissed).toHaveBeenCalled()
  })

  it('has correct layout classes on container', () => {
    const wrapper = mountBanner()
    const container = wrapper.find('.container')
    expect(container.classes()).toContain('mx-auto')
  })
})
