<template>
  <DropdownMenu v-if="shouldShowNotifications">
    <DropdownMenuTrigger>
      <button
        class="relative flex items-center justify-center p-2 rounded-full transition-all duration-150 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-400 group"
        aria-label="Varsler"
      >
        <BellIcon
          class="h-5 w-5 text-gray-700 group-hover:text-blue-600 transition-colors duration-150"
        />
        <span
          v-if="unreadCount && unreadCount > 0"
          class="absolute -top-1.5 -right-1.5 min-w-[1.25rem] h-5 px-1 flex items-center justify-center text-xs font-bold bg-red-500 text-white rounded-full shadow-lg border-2 border-white z-10 animate-pulse"
          style="font-variant-numeric: tabular-nums"
        >
          {{ unreadCount }}
        </span>
      </button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end" class="w-80 max-h-96 overflow-y-auto">
      <div class="p-2 flex justify-between items-center border-b">
        <h3 class="text-sm font-semibold">Varsler</h3>
        <button
          v-if="notifications.length > 0 && unreadCount > 0"
          @click="markAllAsRead"
          class="text-xs text-blue-600 hover:underline"
          :disabled="isMarkingAllAsRead"
        >
          {{ isMarkingAllAsRead ? 'Behandler...' : 'Merk alle som lest' }}
        </button>
      </div>
      <div v-if="isLoading" class="p-4 text-center text-sm text-gray-500">Laster varsler...</div>
      <div v-else-if="error" class="p-4 text-center text-sm text-red-500">
        Kunne ikke laste varsler.
      </div>
      <div v-else-if="notifications.length === 0" class="p-4 text-center text-sm text-gray-500">
        Ingen nye varsler.
      </div>
      <DropdownMenuItem
        v-for="notification in notifications"
        :key="notification.id"
        @click="() => handleNotificationClick(notification)"
        :class="[
          'hover:bg-gray-100 cursor-pointer border-b last:border-b-0 border-gray-200',
          !notification.read
            ? 'bg-blue-50 border-l-4 border-blue-500'
            : 'border-l-4 border-transparent',
        ]"
      >
        <div class="flex items-start p-3 w-full">
          <div
            :class="['mr-3 p-1.5 rounded-full flex-shrink-0', getIconBgClass(notification.type)]"
          >
            <Calendar
              v-if="notification.type === NotificationResponseType.EXPIRY_REMINDER"
              :class="['h-4 w-4', getIconColorClass(notification.type)]"
            />
            <AlertTriangle
              v-else-if="notification.type === NotificationResponseType.EVENT"
              :class="['h-4 w-4', getIconColorClass(notification.type)]"
            />
            <UserIcon
              v-else-if="notification.type === NotificationResponseType.INVITE"
              :class="['h-4 w-4', getIconColorClass(notification.type)]"
            />
            <Bell
              v-else-if="notification.type === NotificationResponseType.INFO"
              :class="['h-4 w-4', getIconColorClass(notification.type)]"
            />
            <Info v-else :class="['h-4 w-4', getIconColorClass(notification.type)]" />
          </div>
          <div class="flex-grow overflow-hidden">
            <div class="flex justify-between w-full items-center">
              <span
                class="font-semibold text-sm text-gray-800 truncate pr-2"
                :title="notification.title"
                >{{ notification.title }}</span
              >
              <span
                :class="[
                  'text-xs ml-1 flex-shrink-0',
                  !notification.read ? 'text-blue-600 font-bold' : 'text-gray-500',
                ]"
              >
                {{ formatDate(notification.createdAt) }}
              </span>
            </div>
            <p class="text-xs text-gray-600 mt-1 whitespace-normal break-words">
              {{ notification.message }}
            </p>
          </div>
        </div>
      </DropdownMenuItem>
      <div class="p-2 border-t mt-1">
        <router-link
          to="/varsler"
          class="text-sm text-blue-600 hover:underline w-full text-center block focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 rounded px-2 py-1"
          tabindex="0"
          @keydown.enter="() => $router.push('/varsler')"
          @keydown.space.prevent="() => $router.push('/varsler')"
        >
          Se alle varsler
        </router-link>
      </div>
    </DropdownMenuContent>
  </DropdownMenu>
</template>

<script lang="ts" setup>
import {
  AlertTriangle,
  Bell,
  Bell as BellIcon,
  Calendar,
  Info,
  User as UserIcon,
} from 'lucide-vue-next'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import type { NotificationResponse } from '@/api/generated/model'
import { NotificationResponseType } from '@/api/generated/model/notificationResponseType'
import { formatDate } from '@/utils/dateUtils'

defineProps<{
  notifications: NotificationResponse[]
  unreadCount: number
  isLoading: boolean
  error: Error | string | null
  isMarkingAllAsRead: boolean
  shouldShowNotifications: boolean
}>()

const emit = defineEmits<{
  (e: 'mark-all-as-read'): void
  (e: 'notification-click', notification: NotificationResponse): void
}>()

const handleNotificationClick = (notification: NotificationResponse) => {
  emit('notification-click', notification)
}

const markAllAsRead = () => {
  emit('mark-all-as-read')
}

const getIconBgClass = (type: string | undefined) => {
  switch (type) {
    case NotificationResponseType.EXPIRY_REMINDER:
      return 'bg-yellow-100'
    case NotificationResponseType.EVENT:
      return 'bg-red-100'
    case NotificationResponseType.INVITE:
      return 'bg-purple-100'
    case NotificationResponseType.INFO:
      return 'bg-blue-100'
    default:
      return 'bg-gray-100'
  }
}

const getIconColorClass = (type: string | undefined) => {
  switch (type) {
    case NotificationResponseType.EXPIRY_REMINDER:
      return 'text-yellow-600'
    case NotificationResponseType.EVENT:
      return 'text-red-600'
    case NotificationResponseType.INVITE:
      return 'text-purple-600'
    case NotificationResponseType.INFO:
      return 'text-blue-600'
    default:
      return 'text-gray-600'
  }
}
</script>
