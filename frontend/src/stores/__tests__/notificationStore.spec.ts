import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useNotificationStore } from '../notificationStore'

describe('useNotificationStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('adds unique notifications and computes unread count', () => {
    const store = useNotificationStore()

    store.addNotification({ id: '1', title: 'A', read: false, createdAt: '2026-01-01T10:00:00Z' })
    store.addNotification({ id: '1', title: 'A', read: false, createdAt: '2026-01-01T10:00:00Z' })
    store.addNotification({ id: '2', title: 'B', read: true, createdAt: '2026-01-01T11:00:00Z' })

    expect(store.notifications).toHaveLength(2)
    expect(store.unreadCount).toBe(1)
  })

  it('marks one or all notifications as read', () => {
    const store = useNotificationStore()

    store.addNotification({ id: '1', title: 'A', read: false, createdAt: '2026-01-01T10:00:00Z' })
    store.addNotification({ id: '2', title: 'B', read: false, createdAt: '2026-01-01T11:00:00Z' })

    store.markNotificationAsRead('1')
    expect(store.isNotificationRead('1')).toBe(true)
    expect(store.unreadCount).toBe(1)

    store.markAllNotificationsAsRead()
    expect(store.unreadCount).toBe(0)
  })
})
