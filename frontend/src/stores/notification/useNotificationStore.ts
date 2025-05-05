// src/stores/useNotificationStore.ts
import { defineStore } from 'pinia';
import { useAuthStore } from '@/stores/useAuthStore.ts';


export interface Notification {
  id: string;
  type: 'crisis' | 'expiry' | 'update';
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  referenceId?: string;
  householdId?: string;
}

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    notifications: [] as Notification[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    unreadCount: (state) => {
      return state.notifications.filter(notification => !notification.read).length;
    },

    sortedNotifications: (state) => {
      return [...state.notifications].sort((a, b) =>
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
    }
  },

  actions: {
    async fetchNotifications() {
      this.loading = true;
      this.error = null;

      try {
        const authStore = useAuthStore();
        if (!authStore.isAuthenticated) {
          this.notifications = [];
          return;
        }

        // This would be replaced with an actual API call
        // const response = await axios.get('/api/notifications');
        // this.notifications = response.data;

        // For now, we'll use mock data
        this.notifications = this.getMockNotifications();
      } catch (error) {
        console.error('Failed to fetch notifications:', error);
        this.error = 'Failed to load notifications';
      } finally {
        this.loading = false;
      }
    },

    markAsRead(notificationId: string) {
      const notification = this.notifications.find(n => n.id === notificationId);
      if (notification) {
        notification.read = true;

        // In a real app, you would send this update to the server
        // axios.patch(`/api/notifications/${notificationId}`, { read: true });
      }
    },

    markAllAsRead() {
      this.notifications.forEach(notification => {
        notification.read = true;
      });

      // In a real app, you would send this update to the server
      // axios.post('/api/notifications/mark-all-read');
    },

    // This function creates mock notification data for development
    getMockNotifications(): Notification[] {
      const now = new Date();
      const yesterday = new Date(now);
      yesterday.setDate(yesterday.getDate() - 1);

      const threeDaysAgo = new Date(now);
      threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);

      return [
        {
          id: '1',
          type: 'crisis',
          title: 'Flomvarsel i ditt område',
          message: 'Det er utstedt flomvarsel for Trondheim kommune. Vær forberedt på mulige evakueringer.',
          read: false,
          createdAt: now.toISOString(),
          referenceId: 'crisis-123'
        },
        {
          id: '2',
          type: 'expiry',
          title: 'Matvarer i beredskapslageret utløper snart',
          message: 'Hermetikk og tørrmat i ditt beredskapslager utløper innen 30 dager. Vennligst sjekk og oppdater.',
          read: false,
          createdAt: yesterday.toISOString(),
          householdId: '123'
        },
        {
          id: '3',
          type: 'update',
          title: 'Oppdatering på krisesituasjon',
          message: 'Flomvarselet for Trondheim kommune er nå nedgradert fra rødt til gult nivå.',
          read: true,
          createdAt: yesterday.toISOString(),
          referenceId: 'crisis-123'
        },
        {
          id: '4',
          type: 'crisis',
          title: 'Ekstremvær på vei',
          message: 'Meteorologisk institutt har varslet om ekstremvær i Trøndelag de neste 48 timene.',
          read: false,
          createdAt: threeDaysAgo.toISOString(),
          referenceId: 'crisis-456'
        },
        {
          id: '5',
          type: 'expiry',
          title: 'Vannforsyning i beredskapslageret må fornyes',
          message: 'Det er på tide å bytte ut vannet i ditt beredskapslager for å sikre friskhet.',
          read: true,
          createdAt: threeDaysAgo.toISOString(),
          householdId: '123'
        }
      ];
    },

    // For real API integration
    async addNotification(notification: Omit<Notification, 'id' | 'createdAt'>) {
      this.loading = true;

      try {
        // const response = await axios.post('/api/notifications', notification);
        // this.notifications.push(response.data);

        // Mock implementation
        const newNotification: Notification = {
          ...notification,
          id: Date.now().toString(),
          createdAt: new Date().toISOString()
        };

        this.notifications.push(newNotification);
      } catch (error) {
        console.error('Failed to add notification:', error);
        this.error = 'Failed to add notification';
      } finally {
        this.loading = false;
      }
    }
  }
});
