/* eslint-disable @typescript-eslint/no-explicit-any */

import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { shallowMount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import Navbar from '@/components/layout/Navbar.vue'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { createPinia, setActivePinia } from 'pinia'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
    currentRoute: {
      value: { path: '/' },
    },
  })),
}))

// Mock the auth store
vi.mock('@/stores/auth/useAuthStore', () => ({
  useAuthStore: vi.fn(),
}))

describe('Navbar', () => {
  beforeEach(() => {
    // Reset the mock before each test
    vi.mocked(useAuthStore).mockReset()

    // Create a fresh Pinia instance for each test
    const pinia = createPinia()
    setActivePinia(pinia)
  })

  it('renders correctly for non-authenticated users', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      isAuthenticated: false,
      isAdmin: false,
      currentUser: null,
      logout: vi.fn(),
    } as any)

    // Use shallowMount instead of mount to avoid child component issues
    const wrapper = shallowMount(Navbar, {
      global: {
        stubs: {
          RouterLink: true, // Stub RouterLink
        },
        mocks: {
          $router: {
            push: vi.fn(),
            currentRoute: {
              value: { path: '/' },
            },
          },
        },
      },
    })

    // Instead of looking for text content, verify the login link exists by its attributes
    const loginLink = wrapper.find('router-link-stub[to="/logg-inn"]')
    expect(loginLink.exists()).toBe(true)
    expect(loginLink.classes()).toContain('bg-blue-600')
    expect(loginLink.classes()).toContain('text-white')

    // Verify mobile menu toggle button exists
    expect(wrapper.find('button.text-gray-700').exists()).toBe(true)
  })

  it('displays admin link for admin users', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      isAuthenticated: true,
      isAdmin: true,
      currentUser: {
        firstName: 'Admin',
        lastName: 'User',
      },
      logout: vi.fn(),
    } as any)

    const wrapper = createComponentWrapper(Navbar)
    expect(wrapper.text()).toContain('Admin')
  })

  it('displays user information for authenticated users', () => {
    vi.mocked(useAuthStore).mockReturnValue({
      isAuthenticated: true,
      isAdmin: false,
      currentUser: {
        firstName: 'John',
        lastName: 'Doe',
      },
      logout: vi.fn(),
    } as any)

    const wrapper = createComponentWrapper(Navbar)
    expect(wrapper.text()).toContain('John Doe')
  })

  it('toggles mobile menu when menu button is clicked', async () => {
    vi.mocked(useAuthStore).mockReturnValue({
      isAuthenticated: false,
      isAdmin: false,
      currentUser: null,
      logout: vi.fn(),
    } as any)

    const wrapper = createComponentWrapper(Navbar)

    // Check initial state (menu closed)
    expect(wrapper.find('.md\\:hidden > div').exists()).toBe(false)

    // Click menu button
    await wrapper.find('button.text-gray-700').trigger('click')

    // Check if menu is now open
    expect(wrapper.find('.md\\:hidden > div').exists()).toBe(true)
  })
})
