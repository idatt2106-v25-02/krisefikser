<script lang="ts">
import {
  Map as MapIcon,
  Home,
  Package,
  Menu as MenuIcon,
  X,
  LogIn,
  User as UserIcon,
  LogOut,
  Bell as BellIcon,
  AlertTriangle,
  Calendar,
  Bell,
  Info,
  RefreshCw,
  ArrowRight,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import { ref, computed, watch } from 'vue'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu'
import {
  useGetNotifications,
  useGetUnreadCount,
  useReadNotification,
  useReadAll,
} from '@/api/generated/notification/notification'
import { useQueryClient } from '@tanstack/vue-query'
import type { NotificationResponse } from '@/api/generated/model'
import type { ErrorType } from '@/api/axios'
import { useNotificationStore } from '@/stores/notificationStore'

export default {
  name: 'AppNavbar',
  components: {
    MapIcon,
    Home,
    Package,
    MenuIcon,
    X,
    LogIn,
    UserIcon,
    LogOut,
    BellIcon,
    AlertTriangle,
    Calendar,
    Bell,
    Info,
    RefreshCw,
    ArrowRight,
    DropdownMenu,
    DropdownMenuTrigger,
    DropdownMenuContent,
    DropdownMenuItem,
  },
  setup() {
    const authStore = useAuthStore()
    const notificationStore = useNotificationStore()
    const isMenuOpen = ref(false)
    const showMobileNotifications = ref(false)

    const queryClient = useQueryClient()

    const notificationParams = computed(() => ({
      pageable: {
        page: 0,
        size: 10,
        sort: ['createdAt,desc'],
      },
    }))

    const {
      data: fetchedNotificationsData,
      isLoading: isLoadingFetchedNotifications,
      isFetching: isFetchingFetchedNotifications,
      error: fetchedNotificationsError,
      refetch: refetchFetchedNotifications,
    } = useGetNotifications(notificationParams, {
      query: {
        enabled: computed(() => authStore.isAuthenticated),
        staleTime: 1000 * 60 * 1,
      },
    })

    const { data: fetchedUnreadCountData, refetch: refetchFetchedUnreadCount } = useGetUnreadCount({
      query: {
        enabled: computed(() => authStore.isAuthenticated),
        staleTime: 1000 * 30,
      },
    })

    const { mutate: mutateReadNotification } = useReadNotification()
    const { mutate: mutateReadAll, isPending: isMarkingAllAsRead } = useReadAll()

    const displayedNotifications = computed(() => {
      const notificationsFromFetch = fetchedNotificationsData.value?.content || []
      return notificationsFromFetch.slice(0, 10)
    })

    const displayUnreadCount = computed(() => fetchedUnreadCountData.value ?? 0)

    const handleNotificationClick = (notification: NotificationResponse) => {
      if (notification.id && !notification.read) {
        notificationStore.markNotificationAsRead(notification.id)
        mutateReadNotification({ id: notification.id }, {
          onSuccess: () => {
            refetchFetchedNotifications()
            refetchFetchedUnreadCount()
          }
        })
      }

      if (notification.url) {
        window.open(notification.url, '_blank')
      }

      showMobileNotifications.value = false
    }

    const markAllAsReadAndRefresh = () => {
      notificationStore.markAllNotificationsAsRead()
      mutateReadAll(undefined, {
        onSuccess: () => {
          refetchFetchedNotifications()
          refetchFetchedUnreadCount()
        }
      })
    }

    const formatDate = (dateInput: string | number[] | undefined): string => {
      if (!dateInput) {
        return '-';
      }

      let notificationDate: Date;

      if (typeof dateInput === 'string') {
        notificationDate = new Date(dateInput);
      } else if (Array.isArray(dateInput) && dateInput.length >= 6) {
        // Jackson LocalDateTime array: [year, month(1-12), day, hour, minute, second, nanoseconds]
        // JavaScript Date constructor: month is 0-indexed (0=Jan, 1=Feb, ...)
        notificationDate = new Date(
          dateInput[0],    // year
          dateInput[1] - 1, // month (adjusting for 0-indexed month)
          dateInput[2],    // day
          dateInput[3],    // hour
          dateInput[4],    // minute
          dateInput[5],    // second
          dateInput[6] ? Math.floor(dateInput[6] / 1000000) : 0 // nanoseconds to milliseconds
        );
      } else {
        console.error('formatDate: Received unparseable date format:', dateInput);
        return 'Invalid date format'; // Handle cases that are neither string nor expected array
      }

      if (isNaN(notificationDate.getTime())) {
        console.error('formatDate: Failed to parse date from input:', dateInput);
        return 'Invalid date';
      }

      const now = new Date();
      const diffInSeconds = Math.floor((now.getTime() - notificationDate.getTime()) / 1000);

      if (diffInSeconds < 0) return 'In the future'; 
      if (diffInSeconds < 5) return 'Nå nettopp'; 
      if (diffInSeconds < 60) return `${diffInSeconds} sek siden`;
      if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} min siden`;
      
      const diffInDays = Math.floor(diffInSeconds / (60 * 60 * 24));

      if (diffInDays === 0) return `I dag, ${notificationDate.getHours().toString().padStart(2, '0')}:${notificationDate.getMinutes().toString().padStart(2, '0')}`;
      if (diffInDays === 1) return 'I går';
      if (diffInDays < 7) {
        const days = ['Søndag', 'Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag'];
        return days[notificationDate.getDay()];
      }
      return `${notificationDate.getDate().toString().padStart(2, '0')}.${(notificationDate.getMonth() + 1).toString().padStart(2, '0')}.${notificationDate.getFullYear()}`;
    };

    watch(
      () => authStore.isAuthenticated,
      (isAuth) => {
        if (isAuth) {
          refetchFetchedNotifications()
          refetchFetchedUnreadCount()
          notificationStore.clearAllNotifications()
        } else {
          notificationStore.clearAllNotifications()
        }
      },
      { immediate: true }
    )

    return {
      authStore,
      isMenuOpen,
      isLoadingNotifications: isLoadingFetchedNotifications,
      isFetchingNotifications: isFetchingFetchedNotifications,
      notificationsError: fetchedNotificationsError,
      notifications: displayedNotifications,
      unreadCountData: displayUnreadCount,
      handleNotificationClick,
      markAllAsRead: markAllAsReadAndRefresh,
      isMarkingAllAsRead,
      formatDate,
      showMobileNotifications,
      refetchNotifications: refetchFetchedNotifications,
    }
  },
}
</script>
<template>
  <nav class="bg-white shadow-sm sticky top-0 z-50">
    <div class="container mx-auto px-4 py-3">
      <div class="flex justify-between items-center">
        <div class="flex items-center">
          <router-link to="/" class="flex items-center">
            <img src="/favicon.ico" alt="Krisefikser.app" class="h-6 w-auto mr-2" />
            <span class="text-xl font-bold text-blue-700">Krisefikser.app</span>
          </router-link>
        </div>

        <div class="hidden md:flex items-center space-x-8">
          <router-link
            v-if="authStore.isAuthenticated && authStore.isAdmin"
            to="/admin"
            class="flex items-center text-gray-700 hover:text-blue-600 transition"
          >
            <UserIcon class="h-5 w-5 mr-1" />
            <span>Admin</span>
          </router-link>
          <router-link
            to="/kart"
            class="flex items-center text-gray-700 hover:text-blue-600 transition"
          >
            <MapIcon class="h-5 w-5 mr-1" />
            <span>Kart</span>
          </router-link>

          <!-- Household link - different destination based on auth status -->
          <router-link
            :to="authStore.isAuthenticated ? '/husstand' : '/bli-med-eller-opprett-husstand'"
            class="flex items-center text-gray-700 hover:text-blue-600 transition"
          >
            <Home class="h-5 w-5 mr-1" />
            <span>Husstand</span>
          </router-link>

          <!-- Emergency supply link - only for authenticated users -->
          <router-link
            to="/husstand/:id/beredskapslager"
            v-if="authStore.isAuthenticated"
            class="flex items-center text-gray-700 hover:text-blue-600 transition"
          >
            <Package class="h-5 w-5 mr-1" />
            <span>Beredskapslager</span>
          </router-link>

          <!-- Show login button when not authenticated -->
          <router-link
            v-if="!authStore.isAuthenticated"
            to="/logg-inn"
            class="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition ml-2"
          >
            Logg inn
          </router-link>

          <!-- Show notifications and user profile when authenticated -->
          <div v-else class="flex items-center space-x-4">
            <!-- Notifications Bell -->
            <DropdownMenu>
              <DropdownMenuTrigger>
                <button
                  class="flex items-center text-gray-700 hover:text-blue-600 transition relative"
                  aria-label="Varsler"
                >
                  <BellIcon class="h-5 w-5" aria-label="Varsler" />
                  <span
                    v-if="unreadCountData && unreadCountData > 0"
                    class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center"
                  >
                    {{ unreadCountData }}
                  </span>
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" class="w-80 max-h-96 overflow-y-auto">
                <div class="p-2 flex justify-between items-center border-b">
                  <h3 class="text-sm font-semibold">Varsler</h3>
                  <button 
                    v-if="notifications.length > 0 && unreadCountData > 0"
                    @click="markAllAsRead"
                    class="text-xs text-blue-600 hover:underline"
                    :disabled="isMarkingAllAsRead"
                  >
                    {{ isMarkingAllAsRead ? 'Behandler...' : 'Merk alle som lest' }}
                  </button>
                </div>
                <div v-if="isLoadingNotifications" class="p-4 text-center text-sm text-gray-500">
                  Laster varsler...
                </div>
                <div v-else-if="notificationsError" class="p-4 text-center text-sm text-red-500">
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
                    'flex flex-col items-start p-3 hover:bg-gray-100 cursor-pointer',
                    !notification.read ? 'bg-blue-50' : ''
                  ]"
                >
                  <div class="flex justify-between w-full">
                    <span class="font-semibold text-sm text-gray-800">{{ notification.title }}</span>
                    <span :class="['text-xs', !notification.read ? 'text-blue-600 font-bold' : 'text-gray-500']">
                      {{ formatDate(notification.createdAt) }}
                    </span>
                  </div>
                  <p class="text-xs text-gray-600 mt-1 whitespace-normal">{{ notification.message }}</p>
                   <span v-if="!notification.read" class="mt-1 h-2 w-2 bg-blue-500 rounded-full"></span>
                </DropdownMenuItem>
                <div class="p-2 border-t mt-1">
                  <router-link to="/varsler" class="text-sm text-blue-600 hover:underline w-full text-center block">
                    Se alle varsler
                  </router-link>
                </div>
              </DropdownMenuContent>
            </DropdownMenu>

            <span class="text-gray-700"
              >{{ authStore.currentUser?.firstName }} {{ authStore.currentUser?.lastName }}</span
            >
            <DropdownMenu>
              <DropdownMenuTrigger>
                <button
                  class="flex items-center text-gray-700 hover:text-blue-600 transition"
                  aria-label="Min profil"
                >
                  <UserIcon class="h-5 w-5" aria-label="Min profil" />
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent>
                <router-link to="/dashboard">
                  <DropdownMenuItem>
                    <UserIcon class="h-5 w-5 mr-2" />
                    <span>Min Profil</span>
                  </DropdownMenuItem>
                </router-link>
                <DropdownMenuItem @select="authStore.logout" variant="destructive">
                  <LogOut class="h-4 w-4 mr-2" />
                  <span>Logg ut</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>


        </div>

        <!-- Mobile menu button -->
        <div class="md:hidden">
          <button @click="isMenuOpen = !isMenuOpen" class="text-gray-700 focus:outline-none">
            <MenuIcon v-if="!isMenuOpen" class="h-6 w-6" />
            <X v-else class="h-6 w-6" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile menu -->
    <div v-if="isMenuOpen" class="md:hidden">
      <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3 bg-gray-50">
        <router-link
          to="/kart"
          class="flex items-center px-3 py-2 rounded text-gray-700 hover:bg-gray-200"
        >
          <MapIcon class="h-5 w-5 mr-2" />
          <span>Kart</span>
        </router-link>

        <!-- Mobile Household link - different destination based on auth status -->
        <router-link
          :to="authStore.isAuthenticated ? '/husstand' : '/bli-med-eller-opprett-husstand'"
          class="flex items-center px-3 py-2 rounded text-gray-700 hover:bg-gray-200"
        >
          <Home class="h-5 w-5 mr-2" />
          <span>Husstand</span>
        </router-link>

        <!-- Emergency supply link - only for authenticated users -->
        <router-link
          v-if="authStore.isAuthenticated"
          to="/husstand/:id/beredskapslager"
          class="flex items-center px-3 py-2 rounded text-gray-700 hover:bg-gray-200"
        >
          <Package class="h-5 w-5 mr-2" />
          <span>Beredskapslager</span>
        </router-link>

        <!-- Notifications for mobile -->
        <div
          v-if="authStore.isAuthenticated"
          class="flex items-center justify-between px-3 py-2 rounded text-gray-700 hover:bg-gray-200 cursor-pointer"
          @click="showMobileNotifications = !showMobileNotifications"
        >
          <div class="flex items-center">
            <BellIcon class="h-5 w-5 mr-2" />
            <span>Varsler</span>
          </div>
          <span
            v-if="unreadCountData && unreadCountData > 0"
            class="bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center"
          >
            {{ unreadCountData }}
          </span>
        </div>

        <!-- Mobile notifications panel -->
        <div
          v-if="showMobileNotifications && authStore.isAuthenticated"
          class="mt-2 bg-white rounded-md shadow-lg p-2"
        >
          <div class="p-2 border-b border-gray-100 flex justify-between items-center">
            <h3 class="font-medium text-gray-900">Varsler</h3>
            <button
              @click="() => refetchNotifications()"
              :disabled="isFetchingNotifications"
              class="text-blue-600 hover:text-blue-800 disabled:opacity-50"
            >
              <RefreshCw class="h-4 w-4" :class="{ 'animate-spin': isFetchingNotifications }" />
            </button>
          </div>
          <div class="max-h-96 overflow-y-auto">
            <div v-if="isLoadingNotifications" class="p-4 text-center text-gray-500">Laster...</div>
            <div v-else-if="notificationsError" class="p-4 text-center text-red-500">
              Feil ved lasting
            </div>
            <div
              v-else-if="!notifications || notifications.length === 0"
              class="p-4 text-center text-gray-500"
            >
              Ingen nye varsler
            </div>
            <div
              v-for="notification in notifications"
              :key="notification.id"
              @click="handleNotificationClick(notification)"
              class="p-3 hover:bg-blue-50 border-b border-gray-100 cursor-pointer"
              :class="{ 'bg-blue-50': !notification.read }"
            >
              <div class="flex items-start gap-2">
                <div
                  class="rounded-full p-2 flex-shrink-0"
                  :class="{
                    'bg-yellow-100 text-yellow-600': (notification.type as string) === 'EXPIRY',
                    'bg-red-100 text-red-600': (notification.type as string) === 'CRISIS',
                    'bg-blue-100 text-blue-600': (notification.type as string) === 'UPDATE',
                  }"
                >
                  <AlertTriangle
                    v-if="(notification.type as string) === 'CRISIS'"
                    class="h-4 w-4"
                  />
                  <Calendar
                    v-else-if="(notification.type as string) === 'EXPIRY'"
                    class="h-4 w-4"
                  />
                  <Bell v-else-if="(notification.type as string) === 'UPDATE'" class="h-4 w-4" />
                  <Info v-else class="h-4 w-4" />
                </div>
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ notification.title || '-' }}</p>
                  <p class="text-xs text-gray-500 mt-1">{{ notification.message || '-' }}</p>
                  <p class="text-xs text-gray-400 mt-1">
                    {{ notification.createdAt ? formatDate(notification.createdAt) : '-' }}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div
            v-if="notifications && notifications.length > 0"
            class="p-2 border-t border-gray-100"
          >
            <button
              @click="markAllAsRead"
              :disabled="isMarkingAllAsRead"
              class="text-sm text-blue-600 hover:text-blue-800 w-full text-center disabled:opacity-50 mb-2"
            >
              {{ isMarkingAllAsRead ? 'Markerer...' : 'Marker alle som lest' }}
            </button>
          </div>
          <div class="p-2 border-t border-gray-100">
            <router-link
              to="/varsler"
              class="text-sm text-blue-600 hover:text-blue-800 flex items-center justify-center"
              @click="showMobileNotifications = false"
            >
              <span>Se alle varsler</span>
              <ArrowRight class="h-3 w-3 ml-1" />
            </router-link>
          </div>
        </div>

        <router-link
          v-if="authStore.isAuthenticated && authStore.isAdmin"
          to="/admin"
          class="flex items-center px-3 py-2 rounded text-gray-700 hover:bg-gray-200"
        >
          <UserIcon class="h-5 w-5 mr-2" />
          <span>Admin</span>
        </router-link>

        <!-- Authentication options -->
        <!-- Show login button when not authenticated -->
        <router-link
          v-if="!authStore.isAuthenticated"
          to="/logg-inn"
          class="flex items-center px-3 py-2 mt-2 rounded bg-blue-600 text-white hover:bg-blue-700"
        >
          <LogIn class="h-5 w-5 mr-2" />
          <span>Logg inn</span>
        </router-link>

        <!-- Show user profile when authenticated -->
        <div v-else class="flex items-center justify-between px-3 py-2 mt-2 rounded text-gray-700">
          <span>{{ authStore.currentUser?.firstName }} {{ authStore.currentUser?.lastName }}</span>
          <DropdownMenu>
            <DropdownMenuTrigger>
              <button class="text-gray-700 hover:text-blue-600">
                <UserIcon class="h-5 w-5" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <router-link to="/dashboard">
                <DropdownMenuItem>
                  <UserIcon class="h-5 w-5 mr-2" />
                  <span>Min Profil</span>
                </DropdownMenuItem>
              </router-link>
              <DropdownMenuItem @select="authStore.logout" variant="destructive">
                <LogOut class="h-4 w-4 mr-2" />
                <span>Logg ut</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </div>
  </nav>
</template>

