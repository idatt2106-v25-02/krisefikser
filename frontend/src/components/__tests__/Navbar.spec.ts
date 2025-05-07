import { createComponentWrapper } from '@/components/__tests__/test-utils'
import { describe, it, expect, vi } from 'vitest'
import Navbar from '@/components/layout/Navbar.vue'
import { useAuthStore } from '@/stores/auth/useAuthStore'

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}))

vi.mock('@/stores/useAuthStore', () => ({
  useAuthStore: vi.fn(() => ({
    isAuthenticated: false,
    isAdmin: false,
    currentUser: null,
    logout: vi.fn(),
  })),
}))

describe('Navbar', () => {
  it('renders correctly for non-authenticated users', () => {
    const wrapper = createComponentWrapper(Navbar)

    // Check if logo is present
    expect(wrapper.find('img[alt="Krisefikser.app"]').exists()).toBe(true)

    // Check if login button is present for non-authenticated users
    expect(wrapper.text()).toContain('Logg inn')
  })

  it('displays admin link for admin users', () => {
    // Mock auth store to return admin user
    vi.mocked(useAuthStore).mockReturnValueOnce({
      isAuthenticated: true,
      isAdmin: true,
      currentUser: {
        firstName: 'Admin',
        lastName: 'User',
      },
      logout: vi.fn(),
      // Add these required Pinia store properties
      $state: {},
      $patch: vi.fn(),
      $reset: vi.fn(),
      $subscribe: vi.fn(),
      $dispose: vi.fn(),
      $onAction: vi.fn(),
      $id: 'auth',
    } as unknown as ReturnType<typeof useAuthStore>)

    const wrapper = createComponentWrapper(Navbar)

    // Check if admin link is present
    expect(wrapper.text()).toContain('Admin')
  })

  it('displays user information for authenticated users', () => {
    // Mock auth store to return authenticated user
    vi.mocked(useAuthStore).mockReturnValueOnce({
      isAuthenticated: true,
      isAdmin: false,
      currentUser: {
        firstName: 'John',
        lastName: 'Doe',
      },
      logout: vi.fn(),
      $state: {},
      $patch: vi.fn(),
      $reset: vi.fn(),
      $subscribe: vi.fn(),
      $dispose: vi.fn(),
      $onAction: vi.fn(),
      $id: 'auth',
    } as unknown as ReturnType<typeof useAuthStore>)

    const wrapper = createComponentWrapper(Navbar)

    // Check if user name is displayed
    expect(wrapper.text()).toContain('John Doe')
  })

  it('toggles mobile menu when menu button is clicked', async () => {
    const wrapper = createComponentWrapper(Navbar)

    // Check initial state (menu closed)
    expect(wrapper.find('.md\\:hidden > div').exists()).toBe(false)

    // Click menu button
    await wrapper.find('button.text-gray-700').trigger('click')

    // Check if menu is now open
    expect(wrapper.find('.md\\:hidden > div').exists()).toBe(true)
  })
})
