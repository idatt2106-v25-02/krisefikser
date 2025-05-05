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
          Uleste ({{ unreadCountData || 0 }})
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
      <div v-if="notifications && hasUnread" class="mb-4 flex justify-end">
        <button
          @click="markAllAsRead"
          :disabled="isMarkingAllAsRead"
          class="text-sm text-blue-600 hover:text-blue-800 flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <CheckIcon class="h-4 w-4 mr-1" />
          {{ isMarkingAllAsRead ? 'Markerer...' : 'Marker alle som lest' }}
        </button>
      </div>

      <!-- Loading state -->
      <div v-if="isLoadingNotifications" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p class="mt-2 text-gray-600">Laster varsler...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="notificationsError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md">
        <p>Kunne ikke laste varsler: {{ notificationsError }}</p>
        <button
          @click="() => refetchNotifications()"
          class="mt-2 text-sm text-red-700 hover:text-red-900 underline"
        >
          Prøv igjen
        </button>
      </div>

      <!-- Empty state -->
      <div v-else-if="!notifications || notifications.length === 0" class="text-center py-8 bg-gray-50 rounded-md">
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
                  'bg-yellow-100 text-yellow-600': (notification.type as string) === 'EXPIRY',
                  'bg-red-100 text-red-600': (notification.type as string) === 'CRISIS',
                  'bg-blue-100 text-blue-600': (notification.type as string) === 'UPDATE'
                }"
              >
                <AlertTriangle v-if="(notification.type as string) === 'CRISIS'" class="h-5 w-5" />
                <Calendar v-else-if="(notification.type as string) === 'EXPIRY'" class="h-5 w-5" />
                <Bell v-else-if="(notification.type as string) === 'UPDATE'" class="h-5 w-5" />
                <Info v-else class="h-5 w-5" />
              </div>

              <div class="flex-grow">
                <div class="flex justify-between items-start">
                  <h3 class="font-medium text-gray-900">{{ notification.title || 'Ingen tittel' }}</h3>
                  <span class="text-xs text-gray-500">{{ notification.createdAt ? formatDate(notification.createdAt) : '' }}</span>
                </div>
                <p class="text-sm text-gray-600 mt-1">{{ notification.message || 'Ingen melding' }}</p>

                <!-- Action button logic remains, potentially using notification.url or needing backend changes -->
                <div class="mt-3">
                  <a
                    v-if="notification.url"
                    :href="notification.url"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="inline-flex items-center text-xs font-medium text-blue-600 hover:text-blue-800"
                    @click.stop
                  >
                    <LinkIcon class="h-3 w-3 mr-1" />
                    <span>Vis detaljer</span>
                  </a>
                   <!-- Fallback/Alternative: Re-add specific links if backend provides referenceId/householdId -->
                  <!--
                  <router-link
                    v-else-if="notification.type === 'CRISIS' && notification.referenceId"
                    :to="`/kart?crisis=${notification.referenceId}`"
                     ...
                  >
                    ...
                  </router-link>
                  -->
                </div>
              </div>
            </div>
          </div>

          <!-- Mark as read button -->
          <div class="px-4 py-2 bg-gray-50 border-t flex justify-end">
            <button
              v-if="!notification.read && notification.id"
              @click.stop="markAsRead(notification.id)"
              :disabled="isMarkingAsRead"
              class="text-xs text-blue-600 hover:text-blue-800 flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <CheckIcon class="h-3 w-3 mr-1" />
              {{ isMarkingAsRead ? 'Markerer...' : 'Marker som lest' }}
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
        <!-- Pagination Controls -->
        <div v-if="totalPages > 1" class="flex justify-center items-center space-x-2 mt-6">
          <button
            @click="currentPage--"
            :disabled="currentPage === 0"
            class="px-3 py-1 border rounded bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
          >Forrige</button>
          <span>Side {{ currentPage + 1 }} av {{ totalPages }}</span>
          <button
            @click="currentPage++"
            :disabled="currentPage >= totalPages - 1"
            class="px-3 py-1 border rounded bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
          >Neste</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import { Bell, Calendar, AlertTriangle, Check as CheckIcon, BellOff as BellOffIcon, Info, Link as LinkIcon } from 'lucide-vue-next';
import {
  useGetNotifications,
  useGetUnreadCount,
  useReadNotification,
  useReadAll
} from '@/api/generated/notification/notification';
import { useQueryClient } from '@tanstack/vue-query';
import type { NotificationResponse, GetNotificationsParams } from '@/api/generated/model';
import type { ErrorType } from '@/api/axios';

const activeFilter = ref('all');

// Pagination State
const currentPage = ref(0);
const pageSize = ref(10);

// Vue Query Client
const queryClient = useQueryClient();

// --- Vue Query Hooks ---
// Fetch Unread Count
const { data: unreadCountData, isLoading: isLoadingUnreadCount } = useGetUnreadCount({
    query: {
        // Optional: Configure staleTime, refetchInterval, etc.
    }
});

// Fetch Notifications (paginated)
const queryParams = computed<GetNotificationsParams>(() => ({
  pageable: {
    page: currentPage.value,
    size: pageSize.value,
    sort: ['createdAt,desc']
  }
}));

const {
  data: notificationsData,
  isLoading: isLoadingNotifications,
  error: notificationsError,
  refetch: refetchNotifications,
} = useGetNotifications(queryParams, {
    query: {
        placeholderData: (previousData) => previousData,
        // Optional: Configure staleTime, etc.
    }
});

// Mutations
const { mutate: mutateReadNotification, isPending: isMarkingAsRead } = useReadNotification({
    mutation: {
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['/api/notifications'] });
            queryClient.invalidateQueries({ queryKey: ['/api/notifications/unread'] });
        },
        onError: (error: ErrorType<unknown>) => {
            console.error('Failed to mark notification as read:', error);
        },
    }
});

const { mutate: mutateReadAll, isPending: isMarkingAllAsRead } = useReadAll({
    mutation: {
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['/api/notifications'] });
            queryClient.invalidateQueries({ queryKey: ['/api/notifications/unread'] });
        },
        onError: (error: ErrorType<unknown>) => {
            console.error('Failed to mark all notifications as read:', error);
        },
    }
});


// --- Computed Properties ---
const notifications = computed(() => notificationsData.value?.content || []);
const totalPages = computed(() => notificationsData.value?.totalPages || 0);

const filteredNotifications = computed(() => {
  const currentNotifications = notifications.value;

  switch (activeFilter.value) {
    case 'unread':
      return currentNotifications.filter(n => !n.read);
    case 'crisis':
      return currentNotifications.filter(n => (n.type as string) === 'CRISIS');
    case 'expiry':
      return currentNotifications.filter(n => (n.type as string) === 'EXPIRY');
    case 'update':
      return currentNotifications.filter(n => (n.type as string) === 'UPDATE');
    default:
      return currentNotifications;
  }
});

// Use dedicated unread count hook data
const hasUnread = computed(() => {
    return (unreadCountData.value ?? 0) > 0;
});

// --- Methods ---
const handleNotificationClick = (notification: NotificationResponse) => {
  // Mark as read first
  if (notification.id && !notification.read) {
    markAsRead(notification.id);
  }

  console.log("Notification clicked:", notification);
  // Routing logic - prefer URL if available
  if (notification.url) {
      window.open(notification.url, '_blank');
  }
  // Add back specific routing if needed and if backend provides necessary IDs
  // else if (notification.type === 'CRISIS' && notification.referenceId) {
  //    router.push(...)
  // }
};

// Call local mutation function
const markAsRead = (notificationId: string) => {
    if (!notificationId) {
        console.error("Notification ID missing");
        return;
    }
    mutateReadNotification({ id: notificationId });
};

// Call local mutation function
const markAllAsRead = () => {
    mutateReadAll();
};

// Reset to first page when filter changes
watch(activeFilter, () => {
  currentPage.value = 0;
});

const formatDate = (dateString: string): string => {
    if (!dateString) return '';
    try {
        const now = new Date();
        const notificationDate = new Date(dateString);

        const diffInSeconds = Math.floor((now.getTime() - notificationDate.getTime()) / 1000);
        const diffInMinutes = Math.floor(diffInSeconds / 60);
        const diffInHours = Math.floor(diffInMinutes / 60);
        const diffInDays = Math.floor(diffInHours / 24);

        if (diffInDays === 0) {
            if (diffInHours > 0) return `${diffInHours}t siden`;
            if (diffInMinutes > 0) return `${diffInMinutes}m siden`;
            return `Nå nettopp`;
        } else if (diffInDays === 1) {
            return `I går, ${notificationDate.getHours().toString().padStart(2, '0')}:${notificationDate.getMinutes().toString().padStart(2, '0')}`;
        } else if (diffInDays < 7) {
            return `${diffInDays}d siden`;
        } else {
            const day = notificationDate.getDate().toString().padStart(2, '0');
            const month = (notificationDate.getMonth() + 1).toString().padStart(2, '0');
            const year = notificationDate.getFullYear();
            return `${day}.${month}.${year}`;
        }
    } catch (e) {
        console.error("Error formatting date:", e);
        return dateString;
    }
};
</script>

<style scoped>
/* Add any specific styles if needed */
</style>
