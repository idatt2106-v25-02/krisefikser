<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGetNotifications, useReadNotification } from '@/api/generated/notification/notification'
import { AlertTriangle, Bell, Calendar, Info, ArrowLeft, Link as LinkIcon, Check as CheckIcon } from 'lucide-vue-next'
import { NotificationResponseType } from '@/api/generated/model/notificationResponseType'

const route = useRoute()
const router = useRouter()
const notificationId = computed(() => route.params.id as string)

// Fetch all notifications and find the one we want
const { data: notificationsData, isLoading, error, refetch } = useGetNotifications({
  pageable: {
    page: 0,
    size: 100, // Large enough to likely include our notification
    sort: ['createdAt,desc'],
  },
})

// Find the specific notification
const notification = computed(() => {
  if (!notificationsData.value?.content) return null
  return notificationsData.value.content.find(n => n.id === notificationId.value)
})

const { mutate: markAsRead, isPending: isMarkingAsRead } = useReadNotification()

const formatDate = (dateInput: string | number[] | undefined): string => {
  if (!dateInput) return '-'
  let notificationDate: Date
  if (typeof dateInput === 'string') {
    notificationDate = new Date(dateInput)
  } else if (Array.isArray(dateInput) && dateInput.length >= 6) {
    notificationDate = new Date(
      dateInput[0], dateInput[1] - 1, dateInput[2], dateInput[3], dateInput[4], dateInput[5], dateInput[6] ? Math.floor(dateInput[6] / 1000000) : 0
    )
  } else {
    return 'Invalid date format'
  }
  if (isNaN(notificationDate.getTime())) return 'Invalid date'
  return notificationDate.toLocaleString('nb-NO', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const getTypeBadge = (type: string | undefined) => {
  switch (type) {
    case NotificationResponseType.EVENT:
      return { text: 'Hendelse', color: 'bg-red-100 text-red-700', icon: AlertTriangle }
    case NotificationResponseType.INVITE:
      return { text: 'Invitasjon', color: 'bg-yellow-100 text-yellow-700', icon: Calendar }
    case NotificationResponseType.INFO:
      return { text: 'Info', color: 'bg-blue-100 text-blue-700', icon: Bell }
    default:
      return { text: 'Info', color: 'bg-gray-100 text-gray-700', icon: Info }
  }
}

const handleMarkAsRead = () => {
  if (notification.value && !notification.value.read && notification.value.id) {
    markAsRead({ id: notification.value.id }, { onSuccess: () => refetch() })
  }
}

const goBack = () => {
  router.push({ name: 'notifications' })
}

const handleNotificationAction = () => {
  if (!notification.value) return

  if (notification.value.url) {
    window.open(notification.value.url, '_blank')
  }
}
</script>

<template>
  <div class="container mx-auto px-4 py-12 max-w-2xl">
    <div class="flex items-center mb-6">
      <button class="text-blue-600 hover:text-blue-800 transition-colors font-medium flex items-center group" @click="goBack">
        <ArrowLeft class="h-5 w-5 mr-2 group-hover:translate-x-[-3px] transition-transform" />
        <span>Tilbake til Varsler</span>
      </button>
    </div>
    <div v-if="isLoading" class="bg-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center py-16">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-4 border-b-blue-500 border-blue-100 mb-4"></div>
      <p class="text-gray-600 font-medium">Laster varsel...</p>
    </div>
    <div v-else-if="error" class="bg-white rounded-lg shadow-lg p-8 border-l-4 border-red-500">
      <div class="flex items-start">
        <div class="flex-shrink-0 mt-0.5">
          <Info class="h-6 w-6 text-red-500" />
        </div>
        <div class="ml-3">
          <h3 class="text-lg font-medium text-red-800">Kunne ikke laste varsel</h3>
          <p class="mt-2 text-red-700">{{ error }}</p>
        </div>
      </div>
    </div>
    <div v-else-if="notification" class="bg-white rounded-lg shadow-lg p-8 relative">
      <div class="absolute top-0 right-0 w-24 h-24 bg-blue-50/70 rounded-bl-full -mt-2 -mr-2 overflow-hidden z-0">
        <component :is="getTypeBadge(notification.type).icon" class="h-10 w-10 text-blue-500 absolute top-6 right-6" />
      </div>
      <div class="relative z-10">
        <h1 class="text-2xl font-bold text-gray-800 pb-2 mb-2">{{ notification.title }}</h1>
        <div class="flex flex-wrap gap-2 mb-4 text-sm">
          <span :class="'inline-flex items-center px-2.5 py-0.5 rounded-full ' + getTypeBadge(notification.type).color">
            <component :is="getTypeBadge(notification.type).icon" class="h-4 w-4 mr-1" />
            {{ getTypeBadge(notification.type).text }}
          </span>
          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full bg-gray-100 text-gray-800">
            <Calendar class="h-4 w-4 mr-1" />
            {{ formatDate(notification.createdAt) }}
          </span>
          <span v-if="notification.read" class="inline-flex items-center px-2.5 py-0.5 rounded-full bg-green-100 text-green-800">
            <CheckIcon class="h-4 w-4 mr-1" /> Lest
          </span>
        </div>
        <div class="text-gray-700 leading-relaxed prose max-w-none mb-6">
          {{ notification.message }}
        </div>
        <div class="flex gap-3">
          <button 
            v-if="!notification.read" 
            @click="handleMarkAsRead" 
            :disabled="isMarkingAsRead" 
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition disabled:opacity-50 flex items-center"
          >
            <CheckIcon class="h-4 w-4 mr-1" />
            {{ isMarkingAsRead ? 'Markerer...' : 'Marker som lest' }}
          </button>
          <button 
            v-if="notification.url"
            @click="handleNotificationAction"
            class="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition flex items-center"
          >
            <LinkIcon class="h-4 w-4 mr-1" />
            Åpne lenke
          </button>
        </div>
      </div>
    </div>
    <div v-else class="bg-white rounded-lg shadow-lg p-8 flex flex-col items-center justify-center py-16">
      <Info class="h-10 w-10 text-gray-400 mb-4" />
      <p class="text-gray-600 text-center text-lg">Varsel ikke funnet.</p>
    </div>
  </div>
</template>

<style scoped>
.prose {
  line-height: 1.6;
}
</style> 