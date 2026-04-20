import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import NotificationView from './NotificationView.vue'

const invalidateQueriesMock = vi.fn()
const setQueryDataMock = vi.fn()

vi.mock('@tanstack/vue-query', () => ({
  useQueryClient: vi.fn(() => ({
    invalidateQueries: invalidateQueriesMock,
    setQueryData: setQueryDataMock,
  })),
}))

const markReadState = new Set<string>()
const markAllNotificationsAsReadMock = vi.fn()
vi.mock('@/stores/notificationStore', () => ({
  useNotificationStore: vi.fn(() => ({
    isNotificationRead: (id: string) => markReadState.has(id),
    markAsRead: (id: string) => markReadState.add(id),
    markAllNotificationsAsRead: markAllNotificationsAsReadMock,
    clear: () => markReadState.clear(),
  })),
}))

const unreadCountRef = ref(2)
const notificationsRef = ref({
  content: [
    {
      id: 'n-1',
      title: 'Ny varsling',
      message: 'Testmelding',
      read: false,
      type: 'CRISIS',
      createdAt: '2026-01-01T10:00:00Z',
    },
    {
      id: 'n-2',
      title: 'Påminnelse',
      message: 'Utløpsdato nærmer seg',
      read: true,
      type: 'EXPIRY',
      createdAt: '2026-01-02T10:00:00Z',
    },
  ],
  totalPages: 1,
  totalElements: 2,
  size: 5,
  number: 0,
})

const mutateReadNotificationMock = vi.fn()
const mutateReadAllMock = vi.fn()

vi.mock('@/api/generated/notification/notification', () => ({
  useGetUnreadCount: vi.fn(() => ({
    data: unreadCountRef,
    refetch: vi.fn(),
  })),
  useGetNotifications: vi.fn(() => ({
    data: notificationsRef,
    isLoading: ref(false),
    error: ref(null),
    refetch: vi.fn(),
  })),
  useReadNotification: vi.fn(() => ({
    mutate: mutateReadNotificationMock,
  })),
  useReadAll: vi.fn(() => ({
    mutate: mutateReadAllMock,
  })),
}))

describe('NotificationView', () => {
  beforeEach(() => {
    markReadState.clear()
    mutateReadNotificationMock.mockClear()
    mutateReadAllMock.mockClear()
    markAllNotificationsAsReadMock.mockClear()
    invalidateQueriesMock.mockClear()
    setQueryDataMock.mockClear()
  })

  it('renders notifications and filters unread', async () => {
    const wrapper = mount(NotificationView, {
      global: {
        stubs: {
          RouterLink: { template: '<a><slot /></a>' },
        },
      },
    })

    expect(wrapper.text()).toContain('Ny varsling')
    expect(wrapper.text()).toContain('Påminnelse')

    const unreadButton = wrapper.findAll('button').find((btn) => btn.text().toLowerCase().includes('ulest'))
    expect(unreadButton).toBeTruthy()
    await unreadButton!.trigger('click')

    expect(wrapper.text()).toContain('Ny varsling')
  })

  it('triggers mark all read action', async () => {
    const wrapper = mount(NotificationView, {
      global: {
        stubs: {
          RouterLink: { template: '<a><slot /></a>' },
        },
      },
    })

    const markAllButton = wrapper
      .findAll('button')
      .find((btn) => btn.text().toLowerCase().includes('marker alle som lest'))
    expect(markAllButton).toBeTruthy()
    await markAllButton!.trigger('click')

    expect(markAllNotificationsAsReadMock).toHaveBeenCalled()
  })
})
