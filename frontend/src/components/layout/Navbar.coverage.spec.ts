import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import Navbar from './Navbar.vue'

const pushMock = vi.fn()
const logoutMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({ push: pushMock })),
  useRoute: vi.fn(() => ({ path: '/kriser' })),
}))

vi.mock('@/stores/auth/useAuthStore', () => ({
  useAuthStore: vi.fn(() => ({
    isAuthenticated: true,
    isAdmin: true,
    currentUser: { id: 'u-1', firstName: 'Kari', lastName: 'Nordmann' },
    logout: logoutMock,
  })),
}))

vi.mock('@/stores/notificationStore', () => ({
  useNotificationStore: vi.fn(() => ({
    unreadCount: 1,
    setUnreadCount: vi.fn(),
    markAsRead: vi.fn(),
    clearAllNotifications: vi.fn(),
    isNotificationRead: vi.fn(() => false),
  })),
}))

vi.mock('@/api/generated/authentication/authentication', () => ({
  useMe: vi.fn(() => ({ data: ref({ notifications: true }) })),
}))

vi.mock('@/api/generated/notification/notification', () => ({
  useGetNotifications: vi.fn(() => ({
    data: ref({
      content: [
        {
          id: 'n-1',
          title: 'Varsling',
          message: 'Test',
          read: false,
          createdAt: '2026-01-01T10:00:00Z',
          type: 'CRISIS',
        },
      ],
    }),
    isLoading: ref(false),
    isFetching: ref(false),
    error: ref(null),
    refetch: vi.fn(),
  })),
  useGetUnreadCount: vi.fn(() => ({
    data: ref({ unreadCount: 1 }),
    refetch: vi.fn(),
  })),
  useReadNotification: vi.fn(() => ({ mutate: vi.fn() })),
  useReadAll: vi.fn(() => ({ mutate: vi.fn(), isPending: ref(false) })),
}))

vi.mock('@/components/notification/NotificationDropdown.vue', () => ({
  default: { name: 'NotificationDropdown', template: '<div>NotificationDropdown</div>' },
}))

describe('Navbar coverage smoke', () => {
  beforeEach(() => {
    pushMock.mockClear()
    logoutMock.mockClear()
    // Needed by navbar audio setup in jsdom
    ;(globalThis as unknown as { AudioContext?: unknown }).AudioContext = class {
      resume = vi.fn()
    }
  })

  it('renders navigation and reacts to menu toggles', async () => {
    const wrapper = mount(Navbar, {
      global: {
        stubs: {
          RouterLink: { template: '<a><slot /></a>' },
          DropdownMenu: { template: '<div><slot /></div>' },
          DropdownMenuTrigger: { template: '<button><slot /></button>' },
          DropdownMenuContent: { template: '<div><slot /></div>' },
          DropdownMenuItem: { template: '<div><slot /></div>' },
        },
      },
    })

    expect(wrapper.text()).toContain('Kriser')
    expect(wrapper.text()).toContain('NotificationDropdown')

    const menuButton = wrapper.findAll('button')[0]
    await menuButton.trigger('click')
    expect(wrapper.text()).toContain('Beredskapslager')
  })
})
