<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto">
      <h1 class="text-2xl font-bold text-gray-900 mb-6">Varsler</h1>

      <!-- Notification filters -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          @click="activeFilter = 'all'"
          class="px-4 py-2 rounded-md text-sm"
          :class="activeFilter === 'all' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
        >
          Alle varsler
        </button>
        <button
          @click="activeFilter = 'unread'"
          class="px-4 py-2 rounded-md text-sm"
          :class="activeFilter === 'unread' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
        >
          Uleste ({{ unreadCount }})
        </button>
        <button
          @click="activeFilter = 'crisis'"
          class="px-4 py-2 rounded-md text-sm"
          :class="activeFilter === 'crisis' ? 'bg-red-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
        >
          Krisevarsler
        </button>
        <button
          @click="activeFilter = 'expiry'"
          class="px-4 py-2 rounded-md text-sm"
          :class="activeFilter === 'expiry' ? 'bg-yellow-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
        >
          Beredskapslager
        </button>
        <button
          @click="activeFilter = 'update'"
          class="px-4 py-2 rounded-md text-sm"
          :class="activeFilter === 'update' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
        >
          Oppdateringer
        </button>
      </div>

      <!-- Mark all as read button -->
      <div v-if="filteredNotifications.length > 0 && hasUnread" class="mb-4 flex justify-end">
        <button
          @click="markAllAsRead"
          class="text-sm text-blue-600 hover:text-blue-800 flex items-center"
        >
          <CheckIcon class="h-4 w-4 mr-1" />
          Marker alle som lest
        </button>
      </div>

      <!-- Loading state -->
      <div v-if="notificationStore.loading" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p class="mt-2 text-gray-600">Laster varsler...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="notificationStore.error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
        <p>{{ notificationStore.error }}</p>
        <button
          @click="notificationStore.fetchNotifications"
          class="mt-2 text-sm text-red-700 hover:text-red-900 underline"
        >
          Prøv igjen
        </button>
      </div>

      <!-- Empty state -->
      <div v-else-if="filteredNotifications.length === 0" class="text-center py-8 bg-gray-50 rounded-md">
        <BellOffIcon class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-2 text-gray-600">Ingen varsler å vise</p>
        <p class="text-sm text-gray-500">
          {{ activeFilter === 'all' ? 'Du har ingen varsler ennå.' : 'Ingen varsler matcher det valgte filteret.' }}
        </p>
      </div>

      <!-- Notifications list -->
      <div v-else class="space-y-4">
        <div
          v-for="notification in filteredNotifications"
          :key="notification.id"
          class="bg-white border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition"
          :class="{ 'border-l-4 border-l-blue-500': !notification.read }"
        >
          <div class="p-4 cursor-pointer" @click="handleNotificationClick(notification)">
            <div class="flex items-start gap-3">
              <!-- Icon based on notification type -->
              <div
                class="rounded-full p-3 flex-shrink-0"
                :class="{
                  'bg-yellow-100 text-yellow-600': notification.type === 'expiry',
                  'bg-red-100 text-red-600': notification.type === 'crisis',
                  'bg-blue-100 text-blue-600': notification.type === 'update'
                }"
              >
                <AlertTriangle v-if="notification.type === 'crisis'" class="h-5 w-5" />
                <Calendar v-else-if="notification.type === 'expiry'" class="h-5 w-5" />
                <Bell v-else class="h-5 w-5" />
              </div>

              <div class="flex-grow">
                <div class="flex justify-between items-start">
                  <h3 class="font-medium text-gray-900">{{ notification.title }}</h3>
                  <span class="text-xs text-gray-500">{{ formatDate(notification.createdAt) }}</span>
                </div>
                <p class="text-sm text-gray-600 mt-1">{{ notification.message }}</p>

                <!-- Action button based on notification type -->
                <div class="mt-3">
                  <router-link
                    v-if="notification.type === 'crisis'"
                    :to="`/kart?crisis=${notification.referenceId}`"
                    class="inline-flex items-center text-xs font-medium text-red-600 hover:text-red-800"
                  >
                    <MapPin class="h-3 w-3 mr-1" />
                    <span>Se på kartet</span>
                  </router-link>

                  <router-link
                    v-else-if="notification.type === 'expiry'"
                    :to="`/husstand/${notification.householdId}/beredskapslager`"
                    class="inline-flex items-center text-xs font-medium text-yellow-600 hover:text-yellow-800"
                  >
                    <Package class="h-3 w-3 mr-1" />
                    <span>Se beredskapslager</span>
                  </router-link>

                  <router-link
                    v-else-if="notification.type === 'update'"
                    :to="`/kart?update=${notification.referenceId}`"
                    class="inline-flex items-center text-xs font-medium text-blue-600 hover:text-blue-800"
                  >
                    <Info class="h-3 w-3 mr-1" />
                    <span>Se oppdatering</span>
                  </router-link>
                </div>
              </div>
            </div>
          </div>

          <!-- Mark as read button -->
          <div class="px-4 py-2 bg-gray-50 border-t flex justify-end">
            <button
              v-if="!notification.read"
              @click.stop="markAsRead(notification.id)"
              class="text-xs text-blue-600 hover:text-blue-800 flex items-center"
            >
              <CheckIcon class="h-3 w-3 mr-1" />
              <span>Marker som lest</span>
            </button>
            <span
              v-else
              class="text-xs text-gray-500 flex items-center"
            >
              <CheckIcon class="h-3 w-3 mr-1" />
              <span>Lest</span>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Bell, Calendar, AlertTriangle, Check as CheckIcon, BellOff as BellOffIcon, MapPin, Package, Info } from 'lucide-vue-next';
import { useNotificationStore } from '@/stores/notification/useNotificationStore';
import type { Notification } from '@/stores/notification/useNotificationStore';

export default {
  name: 'NotificationsPage',
  components: {
    Bell,
    Calendar,
    AlertTriangle,
    CheckIcon,
    BellOffIcon,
    MapPin,
    Package,
    Info
  },
  setup() {
    const notificationStore = useNotificationStore();
    const router = useRouter();
    const activeFilter = ref('all');

    const filteredNotifications = computed(() => {
      const notifications = notificationStore.sortedNotifications;

      switch (activeFilter.value) {
        case 'unread':
          return notifications.filter(n => !n.read);
        case 'crisis':
          return notifications.filter(n => n.type === 'crisis');
        case 'expiry':
          return notifications.filter(n => n.type === 'expiry');
        case 'update':
          return notifications.filter(n => n.type === 'update');
        default:
          return notifications;
      }
    });

    const unreadCount = computed(() => {
      return notificationStore.unreadCount;
    });

    const hasUnread = computed(() => {
      return filteredNotifications.value.some(n => !n.read);
    });

    const handleNotificationClick = (notification: Notification) => {
      markAsRead(notification.id);

      // Route based on notification type
      if (notification.type === 'crisis') {
        router.push(`/kart?crisis=${notification.referenceId}`);
      } else if (notification.type === 'expiry') {
        router.push(`/husstand/${notification.householdId}/beredskapslager`);
      } else if (notification.type === 'update') {
        router.push(`/kart?update=${notification.referenceId}`);
      }
    };

    const markAsRead = (notificationId: string) => {
      notificationStore.markAsRead(notificationId);
    };

    const markAllAsRead = () => {
      notificationStore.markAllAsRead();
    };

    const formatDate = (date: string) => {
      const now = new Date();
      const notificationDate = new Date(date);

      // Calculate the difference in days
      const diffInDays = Math.floor((now.getTime() - notificationDate.getTime()) / (1000 * 60 * 60 * 24));

      if (diffInDays === 0) {
        // If today, show the time
        return `I dag, ${notificationDate.getHours().toString().padStart(2, '0')}:${notificationDate.getMinutes().toString().padStart(2, '0')}`;
      } else if (diffInDays === 1) {
        return 'I går';
      } else if (diffInDays < 7) {
        const days = ['Søndag', 'Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag'];
        return days[notificationDate.getDay()];
      } else {
        // For older notifications, show the date
        return `${notificationDate.getDate().toString().padStart(2, '0')}.${(notificationDate.getMonth() + 1).toString().padStart(2, '0')}.${notificationDate.getFullYear()}`;
      }
    };

    onMounted(() => {
      notificationStore.fetchNotifications();
    });

    return {
      notificationStore,
      activeFilter,
      filteredNotifications,
      unreadCount,
      hasUnread,
      handleNotificationClick,
      markAsRead,
      markAllAsRead,
      formatDate
    };
  }
};
</script>
