// frontend/src/stores/notificationStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { BackendNotification } from '@/types/notification';
// import { useRouter } from 'vue-router'; // If actions need router

// TODO: Import and use actual API calls for marking read status if available
// import { useReadNotification, useReadAll } from '@/api/generated/notification/notification';

export const useNotificationStore = defineStore('notification', () => {
  // const router = useRouter(); // Uncomment if needed for navigation from notifications
  const notifications = ref<BackendNotification[]>([]);

  // TODO: Integrate with actual API mutation hooks from vue-query if available
  // Example: const { mutate: markAsReadMutation } = useReadNotification();
  // Example: const { mutate: markAllAsReadMutation } = useReadAll();

  const unreadCount = computed(() => {
    return notifications.value.filter(n => !n.read).length;
  });

  const sortedNotifications = computed(() => {
    // Show newest first
    return [...notifications.value].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  });

  function addNotification(notification: BackendNotification) {
    // Avoid adding duplicates if WebSocket somehow sends multiple times
    const exists = notifications.value.some(n => n.id === notification.id);
    if (!exists) {
      notifications.value.unshift(notification); // Add to the beginning
      // Optional: Limit the number of notifications stored in memory
      // if (notifications.value.length > 50) { 
      //   notifications.value.pop(); 
      // }
    }
  }

  function markNotificationAsRead(notificationId: string) {
    const notification = notifications.value.find(n => n.id === notificationId);
    if (notification && !notification.read) {
      notification.read = true;
      // TODO: Call backend API to mark as read using actual mutation
      // e.g., markAsReadMutation({ id: notificationId }).catch(err => console.error(err));
      console.log(`TODO: Persist read status for notification ${notificationId} to backend`);
    }
  }

  function markAllNotificationsAsRead() {
    let actuallyMarked = false;
    notifications.value.forEach(n => {
      if (!n.read) {
        n.read = true;
        actuallyMarked = true;
      }
    });
    if (actuallyMarked) {
      // TODO: Call backend API to mark all as read using actual mutation
      // e.g., markAllAsReadMutation({}).catch(err => console.error(err));
      console.log('TODO: Persist all notifications as read to backend');
    }
  }

  function removeNotification(notificationId: string) {
    notifications.value = notifications.value.filter(n => n.id !== notificationId);
    // TODO: Optionally call backend to delete notification if needed, though usually they are just marked read
  }

  function clearAllNotifications() {
    notifications.value = [];
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
  };
}); 