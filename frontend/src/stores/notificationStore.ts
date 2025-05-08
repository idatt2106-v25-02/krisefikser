// frontend/src/stores/notificationStore.ts
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { NotificationResponse } from '@/api/generated/model'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<NotificationResponse[]>([])

  function getTimestamp(createdAt: string | number[] | undefined): number {
    if (!createdAt) return 0 // Or Number.MIN_SAFE_INTEGER to sort undefined last

    let dateObj: Date
    if (typeof createdAt === 'string') {
      dateObj = new Date(createdAt)
    } else if (Array.isArray(createdAt) && createdAt.length >= 6) {
      dateObj = new Date(
        createdAt[0], // year
        createdAt[1] - 1, // month (0-indexed)
        createdAt[2], // day
        createdAt[3], // hour
        createdAt[4], // minute
        createdAt[5], // second
        createdAt[6] ? Math.floor(createdAt[6] / 1000000) : 0, // nanos to millis
      )
    } else {
      return 0 // Or Number.MIN_SAFE_INTEGER or handle error for unparseable format
    }
    const time = dateObj.getTime()
    // console.log(`getTimestamp for ${createdAt}: ${time}, isNaN: ${isNaN(time)}`); // Optional debug
    return time // Returns NaN for invalid dates
  }

  const unreadCount = computed(() => {
    const count = notifications.value.filter((n) => !n.read).length
    return count
  })

  const sortedNotifications = computed(() => {
    return [...notifications.value].sort((a, b) => {
      const timeA = getTimestamp(a.createdAt)
      const timeB = getTimestamp(b.createdAt)

      // Handle NaN (invalid dates) during sort: push them to the end or beginning
      if (isNaN(timeA) && isNaN(timeB)) return 0
      if (isNaN(timeA)) return 1 // timeA is invalid, sort b before a (b comes first)
      if (isNaN(timeB)) return -1 // timeB is invalid, sort a before b (a comes first)

      return timeB - timeA // Sort newest first (descending timestamps)
    })
  })

  function addNotification(notification: NotificationResponse) {
    const exists = notifications.value.some((n) => n.id === notification.id)
    if (!exists) {
      notifications.value.unshift({ ...notification })
    }
  }

  function markNotificationAsRead(notificationId: string) {
    const index = notifications.value.findIndex((n) => n.id === notificationId)
    if (index !== -1 && !notifications.value[index].read) {
      notifications.value[index] = {
        ...notifications.value[index],
        read: true,
      }
    }
  }

  function markAllNotificationsAsRead() {
    let actuallyMarked = false
    const newNotifications = notifications.value.map((n) => {
      if (!n.read) {
        actuallyMarked = true
        return { ...n, read: true }
      }
      return n
    })
    if (actuallyMarked) {
      notifications.value = newNotifications
    }
  }

  function clearAllNotifications() {
    notifications.value = []
  }

  function isNotificationRead(notificationId: string): boolean {
    const notification = notifications.value.find((n) => n.id === notificationId)
    return notification?.read ?? false
  }

  return {
    notifications: sortedNotifications, // Expose sorted notifications
    unreadCount,
    addNotification,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    clearAllNotifications,
    isNotificationRead,
  }
})
