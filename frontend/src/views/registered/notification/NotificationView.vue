<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import {
  AlertTriangle,
  Bell,
  BellOff as BellOffIcon,
  Calendar,
  Check as CheckIcon,
  Info,
  Link as LinkIcon,
} from 'lucide-vue-next'
import {
  useGetNotifications,
  useGetUnreadCount,
  useReadAll,
  useReadNotification,
} from '@/api/generated/notification/notification'
import { useQueryClient } from '@tanstack/vue-query'
import type { NotificationResponse } from '@/api/generated/model'
import { NotificationResponseType } from '@/api/generated/model/notificationResponseType'
import type { ErrorType } from '@/api/axios'
import { useNotificationStore } from '@/stores/notificationStore'

const activeFilter = ref('all')

// Pagination State
const currentPage = ref(0)

// Vue Query Client
const queryClient = useQueryClient()
const notificationStore = useNotificationStore()

// Local state for optimistic updates
const processingNotifications = ref(new Set<string>())
const processingAllNotifications = ref(false)

// Add type for paginated response
interface PaginatedResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

const formatDate = (dateInput: string | number[] | undefined): string => {
  if (!dateInput) {
    return '-'
  }

  let notificationDate: Date

  if (typeof dateInput === 'string') {
    notificationDate = new Date(dateInput)
  } else if (Array.isArray(dateInput) && dateInput.length >= 6) {
    // Jackson LocalDateTime array: [year, month(1-12), day, hour, minute, second, nanoseconds]
    // JavaScript Date constructor: month is 0-indexed (0=Jan, 1=Feb, ...)
    notificationDate = new Date(
      dateInput[0], // year
      dateInput[1] - 1, // month (adjusting for 0-indexed month)
      dateInput[2], // day
      dateInput[3], // hour
      dateInput[4], // minute
      dateInput[5], // second
      dateInput[6] ? Math.floor(dateInput[6] / 1000000) : 0, // nanoseconds to milliseconds
    )
  } else {
    console.error('formatDate: Received unparseable date format:', dateInput)
    return 'Invalid date format' // Handle cases that are neither string nor expected array
  }

  if (isNaN(notificationDate.getTime())) {
    console.error('formatDate: Failed to parse date from input:', dateInput)
    return 'Invalid date'
  }

  const now = new Date()
  const diffInSeconds = Math.floor((now.getTime() - notificationDate.getTime()) / 1000)

  if (diffInSeconds < 0) return 'In the future'
  if (diffInSeconds < 5) return 'Nå nettopp'
  if (diffInSeconds < 60) return `${diffInSeconds} sek siden`
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} min siden`

  const diffInDays = Math.floor(diffInSeconds / (60 * 60 * 24))

  if (diffInDays === 0)
    return `I dag, ${notificationDate.getHours().toString().padStart(2, '0')}:${notificationDate.getMinutes().toString().padStart(2, '0')}`
  if (diffInDays === 1) return 'I går'
  if (diffInDays < 7) {
    const days = ['Søndag', 'Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag']
    return days[notificationDate.getDay()]
  }
  return `${notificationDate.getDate().toString().padStart(2, '0')}.${(notificationDate.getMonth() + 1).toString().padStart(2, '0')}.${notificationDate.getFullYear()}`
}

// Fetch Unread Count
const { data: unreadCountData, refetch: refetchUnreadCount } = useGetUnreadCount({
  query: {
    // Optional: Configure staleTime, refetchInterval, etc.
  },
})

// Fetch Notifications (paginated)
const notificationParams = computed(() => ({
  pageable: {
    page: currentPage.value,
    size: 5,
    sort: ['createdAt,desc'],
  },
}))

const {
  data: notificationsData,
  isLoading: isLoadingNotifications,
  error: notificationsError,
  refetch: refetchNotifications,
} = useGetNotifications(notificationParams)

// Mutations
const { mutate: mutateReadNotification } = useReadNotification({
  mutation: {
    onSuccess: (_, variables) => {
      refetchNotifications()
      refetchUnreadCount()

      if (variables.id) {
        processingNotifications.value.delete(variables.id)
      }
    },
    onError: (error: ErrorType<unknown>, variables) => {
      if (variables.id) {
        processingNotifications.value.delete(variables.id)
      }
      refetchNotifications()
      refetchUnreadCount()
    },
  },
})

const { mutate: mutateReadAll } = useReadAll({
  mutation: {
    onSuccess: () => {
      refetchNotifications()
      refetchUnreadCount()
    },
    onError: (error: ErrorType<unknown>) => {
      console.error('Failed to mark all notifications as read:', error)
      processingAllNotifications.value = false
      refetchNotifications()
      refetchUnreadCount()
    },
  },
})

// --- Computed Properties ---
const notifications = computed(() => notificationsData.value?.content || [])
const totalPages = computed(() => notificationsData.value?.totalPages || 0)

const filteredNotifications = computed(() => {
  let currentNotifications = notifications.value

  // Apply "read" status for notifications in processing and from store
  currentNotifications = currentNotifications.map((n) => {
    if (
      processingAllNotifications.value ||
      (n.id && processingNotifications.value.has(n.id)) ||
      (n.id && notificationStore.isNotificationRead(n.id))
    ) {
      return { ...n, read: true }
    }
    return n
  })

  switch (activeFilter.value) {
    case 'unread':
      return currentNotifications.filter((n) => !n.read)
    case 'crisis':
      return currentNotifications.filter((n) => (n.type as string) === 'CRISIS')
    case 'expiry':
      return currentNotifications.filter((n) => (n.type as string) === 'EXPIRY')
    case 'update':
      return currentNotifications.filter((n) => (n.type as string) === 'UPDATE')
    default:
      return currentNotifications
  }
})

const hasUnread = computed(() => {
  if (processingAllNotifications.value) return false

  const unreadCount = unreadCountData.value ?? 0
  return unreadCount > 0
})

const isMarkingAsRead = (notificationId: string | undefined) => {
  if (!notificationId) return false
  return processingNotifications.value.has(notificationId)
}

const isMarkingAllAsRead = computed(() => {
  return processingAllNotifications.value
})

const handleNotificationClick = (notification: NotificationResponse) => {
  if (notification.id && !notification.read) {
    markAsRead(notification.id)
  }

  console.log('Notification clicked:', notification)
}

// Call local mutation function with optimistic updates
const markAsRead = (notificationId: string) => {
  if (!notificationId) {
    console.error('Notification ID missing')
    return
  }

  // Prevent duplicate processing
  if (processingNotifications.value.has(notificationId)) {
    return
  }

  // Add to processing set for UI indication
  processingNotifications.value.add(notificationId)

  // Update local store
  notificationStore.markNotificationAsRead(notificationId)

  // Optimistically update the UI by updating the local query cache
  const queryKey = ['/api/notifications', notificationParams.value]
  queryClient.setQueryData<PaginatedResponse<NotificationResponse>>(queryKey, (oldData) => {
    if (!oldData || !oldData.content) return oldData

    return {
      ...oldData,
      content: oldData.content.map((n: NotificationResponse) =>
        n.id === notificationId ? { ...n, read: true } : n,
      ),
    }
  })

  // Also optimistically update the unread count
  const unreadCountKey = ['/api/notifications/unread']
  queryClient.setQueryData<number | undefined>(unreadCountKey, (oldData) => {
    if (oldData === undefined) return oldData
    return Math.max(0, oldData - 1)
  })

  // Call the API to mark as read
  mutateReadNotification({ id: notificationId })
}

const markAllAsRead = () => {
  if (processingAllNotifications.value) {
    return
  }

  processingAllNotifications.value = true

  // Update local store
  notificationStore.markAllNotificationsAsRead()

  // Optimistically update UI
  const queryKey = ['/api/notifications', notificationParams.value]
  queryClient.setQueryData<PaginatedResponse<NotificationResponse>>(queryKey, (oldData) => {
    if (!oldData || !oldData.content) return oldData

    return {
      ...oldData,
      content: oldData.content.map((n: NotificationResponse) => ({ ...n, read: true })),
    }
  })

  // Also optimistically update the unread count
  queryClient.setQueryData<number>(['/api/notifications/unread'], 0)

  // Call the API
  mutateReadAll()
}

watch(currentPage, () => {
  refetchNotifications()
})

watch(activeFilter, () => {
  currentPage.value = 0
})
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-4xl mx-auto">
      <h1 class="text-2xl font-bold text-gray-900 mb-6">Varsler</h1>

      <!-- Notification filters -->
      <div class="flex flex-wrap gap-2 mb-6">
        <button
          :class="
            activeFilter === 'all'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
          class="px-4 py-2 rounded-md text-sm"
          @click="activeFilter = 'all'"
        >
          Alle varsler
        </button>
        <button
          :class="
            activeFilter === 'unread'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
          class="px-4 py-2 rounded-md text-sm"
          @click="activeFilter = 'unread'"
        >
          Uleste ({{ unreadCountData || 0 }})
        </button>
        <button
          :class="
            activeFilter === 'crisis'
              ? 'bg-red-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
          class="px-4 py-2 rounded-md text-sm"
          @click="activeFilter = 'crisis'"
        >
          Krisevarsler
        </button>
        <button
          :class="
            activeFilter === 'expiry'
              ? 'bg-yellow-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
          class="px-4 py-2 rounded-md text-sm"
          @click="activeFilter = 'expiry'"
        >
          Beredskapslager
        </button>
        <button
          :class="
            activeFilter === 'update'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
          class="px-4 py-2 rounded-md text-sm"
          @click="activeFilter = 'update'"
        >
          Oppdateringer
        </button>
      </div>

      <!-- Mark all as read button -->
      <div v-if="notifications && hasUnread" class="mb-4 flex justify-end">
        <button
          :disabled="isMarkingAllAsRead"
          class="text-sm text-blue-600 hover:text-blue-800 flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
          @click="markAllAsRead"
        >
          <CheckIcon class="h-4 w-4 mr-1" />
          {{ isMarkingAllAsRead ? 'Markerer...' : 'Marker alle som lest' }}
        </button>
      </div>

      <!-- Loading state -->
      <div v-if="isLoadingNotifications" class="text-center py-8">
        <div
          class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
        ></div>
        <p class="mt-2 text-gray-600">Laster varsler...</p>
      </div>

      <!-- Error state -->
      <div
        v-else-if="notificationsError"
        class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md"
      >
        <p>Kunne ikke laste varsler: {{ notificationsError }}</p>
        <button
          class="mt-2 text-sm text-red-700 hover:text-red-900 underline"
          @click="() => refetchNotifications()"
        >
          Prøv igjen
        </button>
      </div>

      <!-- Empty state -->
      <div
        v-else-if="!notifications || notifications.length === 0"
        class="text-center py-8 bg-gray-50 rounded-md"
      >
        <BellOffIcon class="mx-auto h-12 w-12 text-gray-400" />
        <p class="mt-2 text-gray-600">Ingen varsler å vise</p>
        <p class="text-sm text-gray-500">
          {{
            activeFilter === 'all'
              ? 'Du har ingen varsler ennå.'
              : 'Ingen varsler matcher det valgte filteret.'
          }}
        </p>
      </div>

      <!-- Notifications list -->
      <div v-else class="space-y-4">
        <div
          v-for="notification in filteredNotifications"
          :key="notification.id"
          :class="{ 'border-l-4 border-l-blue-500': !notification.read }"
          class="bg-white border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition"
        >
          <div class="p-4 cursor-pointer" @click="handleNotificationClick(notification)">
            <div class="flex items-start gap-3">
              <!-- Icon based on notification type -->
              <div
                :class="{
                  'bg-yellow-100 text-yellow-600': (notification.type as string) === 'EXPIRY',
                  'bg-red-100 text-red-600': (notification.type as string) === 'CRISIS',
                  'bg-blue-100 text-blue-600': (notification.type as string) === 'UPDATE',
                }"
                class="rounded-full p-3 flex-shrink-0"
              >
                <AlertTriangle v-if="(notification.type as string) === 'CRISIS'" class="h-5 w-5" />
                <Calendar v-else-if="(notification.type as string) === 'EXPIRY'" class="h-5 w-5" />
                <Bell v-else-if="(notification.type as string) === 'UPDATE'" class="h-5 w-5" />
                <Info v-else class="h-5 w-5" />
              </div>

              <div class="flex-grow">
                <div class="flex justify-between items-start">
                  <h3 class="font-medium text-gray-900">
                    {{ notification.title || 'Ingen tittel' }}
                  </h3>
                  <span class="text-xs text-gray-500">{{
                    notification.createdAt ? formatDate(notification.createdAt) : ''
                  }}</span>
                </div>
                <div class="text-gray-700 leading-relaxed prose max-w-none mb-6">
                  {{ notification.message }}
                </div>

                <!-- Action button logic -->
                <div class="mt-3 flex gap-2">
                  <router-link
                    v-if="notification.id"
                    :to="{ name: 'notification-detail', params: { id: notification.id } }"
                    class="inline-flex items-center text-xs font-medium text-blue-600 hover:text-blue-800"
                  >
                    <LinkIcon class="h-3 w-3 mr-1" />
                    <span>Vis detaljer</span>
                  </router-link>

                  <!-- Dynamic action button based on notification type -->
                  <router-link
                    v-if="
                      notification.type === NotificationResponseType.EVENT && notification.eventId
                    "
                    :to="{ name: 'event-detail', params: { id: notification.eventId } }"
                    class="inline-flex items-center text-xs font-medium text-blue-600 hover:text-blue-800"
                    @click.stop
                  >
                    <LinkIcon class="h-3 w-3 mr-1" />
                    <span>Gå til hendelse</span>
                  </router-link>

                  <router-link
                    v-else-if="
                      notification.type === NotificationResponseType.INVITE &&
                      notification.householdId
                    "
                    :to="{ name: 'household', params: { id: notification.householdId } }"
                    class="inline-flex items-center text-xs font-medium text-blue-600 hover:text-blue-800"
                    @click.stop
                  >
                    <LinkIcon class="h-3 w-3 mr-1" />
                    <span>Gå til husstand</span>
                  </router-link>

                  <router-link
                    v-else-if="
                      notification.type === NotificationResponseType.INFO && notification.itemId
                    "
                    :to="{ name: 'notifications' }"
                    class="inline-flex items-center text-xs font-medium text-blue-600 hover:text-blue-800"
                    @click.stop
                  >
                    <LinkIcon class="h-3 w-3 mr-1" />
                    <span>Mer informasjon</span>
                  </router-link>
                </div>
              </div>
            </div>
          </div>

          <!-- Mark as read button -->
          <div class="px-4 py-2 bg-gray-50 border-t flex justify-end">
            <button
              v-if="!notification.read && notification.id"
              :disabled="notification.id ? isMarkingAsRead(notification.id) : false"
              class="text-xs text-blue-600 hover:text-blue-800 flex items-center disabled:opacity-50 disabled:cursor-not-allowed"
              @click.stop="notification.id && markAsRead(notification.id)"
            >
              <CheckIcon class="h-3 w-3 mr-1" />
              {{
                notification.id && isMarkingAsRead(notification.id)
                  ? 'Markerer...'
                  : 'Marker som lest'
              }}
            </button>
            <span v-else class="text-xs text-gray-500 flex items-center">
              <CheckIcon class="h-3 w-3 mr-1" />
              <span>Lest</span>
            </span>
          </div>
        </div>
        <!-- Pagination Controls -->
        <div v-if="totalPages > 1" class="flex justify-center items-center space-x-4 mt-8">
          <button
            :disabled="currentPage === 0"
            class="inline-flex items-center px-4 py-2 text-sm font-medium transition-colors rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
            :class="[
              currentPage === 0
                ? 'bg-gray-100 text-gray-400'
                : 'bg-white text-gray-700 hover:bg-gray-50 hover:text-blue-600 border border-gray-200',
            ]"
            @click="currentPage--"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4 mr-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 19l-7-7 7-7"
              />
            </svg>
            Forrige
          </button>
          <div class="flex items-center gap-1 text-sm">
            <span class="font-medium text-gray-700">Side</span>
            <span class="px-3 py-1 rounded-md bg-blue-50 text-blue-700 font-medium">
              {{ currentPage + 1 }}
            </span>
            <span class="font-medium text-gray-700">av {{ totalPages }}</span>
          </div>
          <button
            :disabled="currentPage >= totalPages - 1"
            class="inline-flex items-center px-4 py-2 text-sm font-medium transition-colors rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
            :class="[
              currentPage >= totalPages - 1
                ? 'bg-gray-100 text-gray-400'
                : 'bg-white text-gray-700 hover:bg-gray-50 hover:text-blue-600 border border-gray-200',
            ]"
            @click="currentPage++"
          >
            Neste
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4 ml-1"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
