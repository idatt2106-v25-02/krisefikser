// frontend/src/stores/notificationStore.ts
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { NotificationResponse } from '@/api/generated/model'
// import { useRouter } from 'vue-router'; // If actions need router

// TODO: Import and use actual API calls for marking read status if available
// import { useReadNotification, useReadAll } from '@/api/generated/notification/notification';

export const useNotificationStore = defineStore('notification', () => {
  // const router = useRouter(); // Uncomment if needed for navigation from notifications
  const notifications = ref<NotificationResponse[]>([])

  // TODO: Integrate with actual API mutation hooks from vue-query if available
  // Example: const { mutate: markAsReadMutation } = useReadNotification();
  // Example: const { mutate: markAllAsReadMutation } = useReadAll();

  // Helper function to convert createdAt to a sortable timestamp
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
    console.log('[NotificationStore] Calculated unreadCount:', count) // DEBUG for unreadCount
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
    console.log(
      '[NotificationStore] Attempting to add notification:',
      JSON.parse(JSON.stringify(notification)),
    ) // DEBUG incoming notification
    // Avoid adding duplicates if WebSocket somehow sends multiple times
    const exists = notifications.value.some((n) => n.id === notification.id)
    if (!exists) {
      notifications.value.unshift({ ...notification }) // Ensure reactivity by spreading
      console.log(
        '[NotificationStore] Notification added. Current count:',
        notifications.value.length,
      )
      // Optional: Limit the number of notifications stored in memory
      // if (notifications.value.length > 50) {
      //   notifications.value.pop();
      // }
    } else {
      console.log('[NotificationStore] Notification already exists, not adding:', notification.id)
    }
  }

  function markNotificationAsRead(notificationId: string) {
    const index = notifications.value.findIndex((n) => n.id === notificationId)
    if (index !== -1 && !notifications.value[index].read) {
      // Ensure reactivity by creating a new object for the updated item
      notifications.value[index] = {
        ...notifications.value[index],
        read: true,
      }
      // Alternatively, if the above doesn't robustly trigger updates for computed properties
      // that depend on the array content (like unreadCount), force replace the array element:
      // const newArray = [...notifications.value];
      // newArray[index] = { ...newArray[index], read: true };
      // notifications.value = newArray;
      console.log(`[NotificationStore] Marked as read (local): ${notificationId}`)
      // The backend call (Vue Query mutation) is handled in Navbar.vue where this is called
      // TODO: Ensure the vue-query mutation success also invalidates any queries
      // that might populate this store if it were to fetch its own data.
    }
  }

  function markAllNotificationsAsRead() {
    let actuallyMarked = false
    // Create a new array to ensure reactivity when multiple items change
    const newNotifications = notifications.value.map((n) => {
      if (!n.read) {
        actuallyMarked = true
        return { ...n, read: true }
      }
      return n
    })
    if (actuallyMarked) {
      notifications.value = newNotifications
      console.log('[NotificationStore] All marked as read (local)')
      // The backend call (Vue Query mutation) is handled in Navbar.vue
    }
  }

  function removeNotification(notificationId: string) {
    notifications.value = notifications.value.filter((n) => n.id !== notificationId)
    // TODO: Optionally call backend to delete notification if needed, though usually they are just marked read
  }

  function clearAllNotifications() {
    notifications.value = []
    // TODO: This is a local clear. Decide if it should also interact with backend (e.g., delete all)
  }

  // Placeholder for fetching initial/archived notifications - your NotificationView.vue does this already.
  // This store can primarily handle NEW notifications via WebSocket and manage their immediate state.

  return {
    notifications: sortedNotifications, // Expose sorted notifications
    unreadCount,
    addNotification,
    markNotificationAsRead,
    markAllNotificationsAsRead,
    removeNotification,
    clearAllNotifications,
  }
})
