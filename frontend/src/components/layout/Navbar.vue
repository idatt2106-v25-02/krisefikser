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
  ListChecks,
  BookText,
  Bell as BellIcon,
  AlertTriangle,
  Calendar,
  Bell,
  Info,
  RefreshCw,
  ArrowRight,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu'
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  useGetNotifications,
  useGetUnreadCount,
  useReadNotification,
  useReadAll,
} from '@/api/generated/notification/notification'
import type { NotificationResponse } from '@/api/generated/model'
import { NotificationResponseType } from '@/api/generated/model/notificationResponseType'
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
    DropdownMenu,
    DropdownMenuTrigger,
    DropdownMenuContent,
    DropdownMenuItem,
    ListChecks,
    BookText,
    BellIcon,
    AlertTriangle,
    Calendar,
    Bell,
    Info,
    RefreshCw,
    ArrowRight,
  },
  setup() {
    const authStore = useAuthStore()
    const notificationStore = useNotificationStore()
    const router = useRouter()
    const route = useRoute()
    const isMenuOpen = ref(false)
    const showMobileNotifications = ref(false)
    const audioPlayerRef = ref<HTMLAudioElement | null>(null)

    const allNavItems = computed(() => [
      {
        label: 'Admin',
        to: '/admin',
        icon: UserIcon,
        show: authStore.isAuthenticated && authStore.isAdmin,
      },
      {
        label: 'Kart',
        to: '/kart',
        icon: MapIcon,
        show: true,
      },
      {
        label: 'Kriser',
        to: '/kriser',
        icon: ListChecks,
        show: true,
      },
      {
        label: 'Husstand',
        to: authStore.isAuthenticated ? '/husstand' : '/bli-med-eller-opprett-husstand',
        icon: Home,
        show: true,
      },
      {
        label: 'Beredskapslager',
        to: '/husstand/beredskapslager',
        icon: Package,
        show: authStore.isAuthenticated,
      },
    ])

    // Filtered nav items to display (pre-filters the show condition)
    const filteredNavItems = computed(() => allNavItems.value.filter((item) => item.show))

    // Check if a route is active - exact match only
    const isActive = (path: string) => {
      return route.path === path
    }

    const notificationParams = computed(() => ({
      pageable: {
        page: 0,
        size: 5,
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
      return notificationsFromFetch
        .slice(0, 5)
        .sort((a, b) => {
          const dateA = new Date(Array.isArray(a.createdAt) ? new Date(a.createdAt[0], a.createdAt[1] -1, a.createdAt[2], a.createdAt[3], a.createdAt[4], a.createdAt[5]).toISOString() : a.createdAt || 0).getTime();
          const dateB = new Date(Array.isArray(b.createdAt) ? new Date(b.createdAt[0], b.createdAt[1] -1, b.createdAt[2], b.createdAt[3], b.createdAt[4], b.createdAt[5]).toISOString() : b.createdAt || 0).getTime();
          return dateB - dateA;
        });
    })

    const displayUnreadCount = computed(() => fetchedUnreadCountData.value ?? 0)

    watch(displayUnreadCount, (newCount, oldCount) => {
      if (newCount !== undefined && oldCount !== undefined && newCount > oldCount) {
        if (audioPlayerRef.value) {
          audioPlayerRef.value.play().catch(error => {
            console.warn("Audio play was prevented. User interaction might be required.", error);
          });
        }
        if (navigator.vibrate) {
          navigator.vibrate(200);
        }
      }
    });

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

      if (notification.type === NotificationResponseType.EXPIRY_REMINDER) {
        const currentUser = authStore.currentUser
        const householdId = currentUser && 'activeHouseholdId' in currentUser
          ? (currentUser as any).activeHouseholdId
          : undefined;

        if (householdId) {
          router.push(`/husstand/${householdId}/beredskapslager`);
          showMobileNotifications.value = false;
          return;
        }
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
      if (!dateInput) return '-';

      let notificationDate: Date;

      if (typeof dateInput === 'string') {
        notificationDate = new Date(dateInput);
      } else if (Array.isArray(dateInput) && dateInput.length >= 6) {
        notificationDate = new Date(
          dateInput[0],
          dateInput[1] - 1,
          dateInput[2],
          dateInput[3],
          dateInput[4],
          dateInput[5],
          dateInput[6] ? Math.floor(dateInput[6] / 1000000) : 0
        );
      } else {
        return 'Invalid date format';
      }

      if (isNaN(notificationDate.getTime())) {
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

    const getIconBgClass = (type: string | undefined) => {
      switch (type) {
        case 'EXPIRY_REMINDER': return 'bg-yellow-100';
        case 'CRISIS_UPDATE':
        case 'NEARBY_CRISIS': return 'bg-red-100';
        case 'HOUSEHOLD_INVITE': return 'bg-purple-100';
        case 'SYSTEM_WIDE': return 'bg-blue-100';
        default: return 'bg-gray-100';
      }
    };

    const getIconColorClass = (type: string | undefined) => {
      switch (type) {
        case 'EXPIRY_REMINDER': return 'text-yellow-600';
        case 'CRISIS_UPDATE':
        case 'NEARBY_CRISIS': return 'text-red-600';
        case 'HOUSEHOLD_INVITE': return 'text-purple-600';
        case 'SYSTEM_WIDE': return 'text-blue-600';
        default: return 'text-gray-600';
      }
    };

    // Add route watcher in setup to replace the options API version
    watch(() => route.path, () => {
      isMenuOpen.value = false
    });

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
      audioPlayerRef,
      getIconBgClass,
      getIconColorClass,
      NotificationResponseType,
      filteredNavItems,
      isActive,
    }
  }
}
</script>
<template>
  <nav class="bg-white shadow-sm sticky top-0 z-50">
    <audio ref="audioPlayerRef" src="/sounds/notification.mp3" preload="auto"></audio>
    <div class="container mx-auto px-4 py-4">
      <div class="flex justify-between items-center">
        <div class="flex items-center">
          <router-link to="/" class="flex items-center">
            <img src="/favicon.ico" alt="Krisefikser.app" class="h-5 w-auto mr-2" />
            <span class="text-lg font-bold text-blue-700">Krisefikser.app</span>
          </router-link>
        </div>

        <div class="hidden md:flex items-center space-x-8">
          <router-link
            v-for="item in filteredNavItems"
            :key="item.label"
            :to="item.to"
            :class="[
              'flex items-center transition text-sm',
              isActive(item.to) ? 'text-blue-600 font-medium' : 'text-gray-700 hover:text-blue-600',
            ]"
          >
            <component :is="item.icon" class="h-4 w-4 mr-1" />
            <span>{{ item.label }}</span>
          </router-link>

          <template v-if="!authStore.isAuthenticated">
            <router-link
              to="/logg-inn"
              class="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition ml-2"
            >
              Logg inn
            </router-link>
          </template>
          <template v-else>
            <div class="flex items-center space-x-2">
              <!-- Notifications -->
              <DropdownMenu>
                <DropdownMenuTrigger>
                  <button
                    class="relative flex items-center justify-center p-2 rounded-full transition-all duration-150 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-400 group"
                    aria-label="Varsler"
                  >
                    <BellIcon class="h-5 w-5 text-gray-700 group-hover:text-blue-600 transition-colors duration-150" />
                    <span
                      v-if="unreadCountData && unreadCountData > 0"
                      class="absolute -top-1.5 -right-1.5 min-w-[1.25rem] h-5 px-1 flex items-center justify-center text-xs font-bold bg-red-500 text-white rounded-full shadow-lg border-2 border-white z-10 animate-pulse"
                      style="font-variant-numeric: tabular-nums;"
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
                      'hover:bg-gray-100 cursor-pointer border-b last:border-b-0 border-gray-200',
                      !notification.read ? 'bg-blue-50 border-l-4 border-blue-500' : 'border-l-4 border-transparent'
                    ]"
                  >
                    <div class="flex items-start p-3 w-full">
                      <div :class="['mr-3 p-1.5 rounded-full flex-shrink-0', getIconBgClass(notification.type)]">
                        <Calendar v-if="notification.type === NotificationResponseType.EXPIRY_REMINDER" :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                        <AlertTriangle v-else-if="notification.type === NotificationResponseType.EVENT " :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                        <UserIcon v-else-if="notification.type === NotificationResponseType.INVITE" :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                        <Bell v-else-if="notification.type === NotificationResponseType.INFO" :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                        <Info v-else :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                      </div>
                      <div class="flex-grow overflow-hidden">
                        <div class="flex justify-between w-full items-center">
                          <span class="font-semibold text-sm text-gray-800 truncate pr-2" :title="notification.title">{{ notification.title }}</span>
                          <span :class="['text-xs ml-1 flex-shrink-0', !notification.read ? 'text-blue-600 font-bold' : 'text-gray-500']">
                            {{ formatDate(notification.createdAt) }}
                          </span>
                        </div>
                        <p class="text-xs text-gray-600 mt-1 whitespace-normal break-words">{{ notification.message }}</p>
                      </div>
                    </div>
                  </DropdownMenuItem>
                  <div class="p-2 border-t mt-1">
                    <router-link to="/varsler" class="text-sm text-blue-600 hover:underline w-full text-center block">
                      Se alle varsler
                    </router-link>
                  </div>
                </DropdownMenuContent>
              </DropdownMenu>

              <!-- User Profile -->
              <DropdownMenu>
                <DropdownMenuTrigger>
                  <button
                    :class="[
                      'flex items-center gap-2 px-3 py-1.5 rounded-md border transition-all duration-200 shadow-sm',
                      isActive('/dashboard')
                        ? 'text-blue-700 border-blue-300 bg-blue-50/70'
                        : 'text-gray-700 border-gray-200 hover:text-blue-600 hover:border-blue-200 hover:bg-blue-50/50',
                    ]"
                    aria-label="Min profil"
                  >
                    <UserIcon class="h-4 w-4 flex-shrink-0" />
                    <span class="font-medium text-sm">
                      {{ authStore.currentUser?.firstName }}
                      {{ authStore.currentUser?.lastName }}
                    </span>
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent>
                  <router-link to="/dashboard">
                    <DropdownMenuItem
                      :class="{ 'bg-blue-50 text-blue-600': isActive('/dashboard') }"
                    >
                      <UserIcon class="h-5 w-5 mr-2" />
                      <span>Min Profil</span>
                    </DropdownMenuItem>
                  </router-link>
                  <router-link v-if="authStore.isAuthenticated" to="/mine-refleksjoner">
                    <DropdownMenuItem
                      :class="{ 'bg-blue-50 text-blue-600': isActive('/mine-refleksjoner') }"
                    >
                      <BookText class="h-5 w-5 mr-2" />
                      <span>Mine Refleksjoner</span>
                    </DropdownMenuItem>
                  </router-link>
                  <DropdownMenuItem @select="authStore.logout" variant="destructive">
                    <LogOut class="h-4 w-4 mr-2" />
                    <span>Logg ut</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </template>
        </div>

        <div class="md:hidden">
          <button @click="isMenuOpen = !isMenuOpen" class="text-gray-700 focus:outline-none">
            <MenuIcon v-if="!isMenuOpen" class="h-6 w-6" />
            <X v-else class="h-6 w-6" />
          </button>
        </div>
      </div>
    </div>

    <div v-if="isMenuOpen" class="md:hidden bg-gray-50">
      <div class="container mx-auto px-4 pt-2 pb-3 space-y-1">
        <router-link
          v-for="item in filteredNavItems"
          :key="item.label"
          :to="item.to"
          :class="[
            'flex items-center px-3 py-2 rounded',
            isActive(item.to)
              ? 'text-blue-600 font-medium bg-blue-50 text-sm'
              : 'text-gray-700 hover:bg-gray-200 text-sm',
          ]"
        >
          <component :is="item.icon" class="h-4 w-4 mr-2" />
          <span>{{ item.label }}</span>
        </router-link>

        <!-- Mobile Notifications -->
        <div
          v-if="authStore.isAuthenticated"
          class="flex items-center justify-between px-3 py-2 rounded text-gray-700 hover:bg-gray-200 cursor-pointer relative"
          @click="showMobileNotifications = !showMobileNotifications"
        >
          <div class="flex items-center">
            <span class="relative flex items-center justify-center">
              <BellIcon class="h-5 w-5 text-gray-700 group-hover:text-blue-600 transition-colors duration-150" />
              <span
                v-if="unreadCountData && unreadCountData > 0"
                class="absolute -top-1.5 -right-1.5 min-w-[1.25rem] h-5 px-1 flex items-center justify-center text-xs font-bold bg-red-500 text-white rounded-full shadow-lg border-2 border-white z-10 animate-pulse"
                style="font-variant-numeric: tabular-nums;"
              >
                {{ unreadCountData }}
              </span>
            </span>
            <span class="ml-2">Varsler</span>
          </div>
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
                  :class="getIconBgClass(notification.type)"
                >
                  <Calendar v-if="notification.type === NotificationResponseType.EXPIRY_REMINDER" :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                  <AlertTriangle v-else-if="notification.type === NotificationResponseType.EVENT" :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                  <UserIcon v-else-if="notification.type === NotificationResponseType.INVITE" :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                  <Bell v-else-if="notification.type === NotificationResponseType.INFO" :class="['h-4 w-4', getIconColorClass(notification.type)]" />
                  <Info v-else :class="['h-4 w-4', getIconColorClass(notification.type)]" />
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

        <template v-if="!authStore.isAuthenticated">
          <router-link
            to="/logg-inn"
            class="flex items-center px-3 py-2 mt-2 rounded bg-blue-600 text-white hover:bg-blue-700"
          >
            <LogIn class="h-5 w-5 mr-2" />
            <span>Logg inn</span>
          </router-link>
        </template>
        <template v-else>
          <div class="flex items-center px-3 py-2 mt-2 rounded text-gray-700">
            <DropdownMenu>
              <DropdownMenuTrigger>
                <button
                  :class="[
                    'flex items-center gap-2 w-full px-3 py-2 rounded-md border transition-all duration-200',
                    isActive('/dashboard')
                      ? 'text-blue-700 border-blue-300 bg-blue-50/70'
                      : 'text-gray-700 border-gray-200 hover:text-blue-600 hover:border-blue-200 hover:bg-blue-50/50',
                  ]"
                >
                  <UserIcon class="h-4 w-4 flex-shrink-0" />
                  <span class="font-medium text-sm">
                    {{ authStore.currentUser?.firstName }}
                    {{ authStore.currentUser?.lastName }}
                  </span>
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent>
                <router-link to="/dashboard">
                  <DropdownMenuItem
                    :class="{ 'bg-blue-50 text-blue-600': isActive('/dashboard') }"
                  >
                    <UserIcon class="h-5 w-5 mr-2" />
                    <span>Min Profil</span>
                  </DropdownMenuItem>
                </router-link>
                <router-link v-if="authStore.isAuthenticated" to="/mine-refleksjoner">
                  <DropdownMenuItem
                    :class="{ 'bg-blue-50 text-blue-600': isActive('/mine-refleksjoner') }"
                  >
                    <BookText class="h-5 w-5 mr-2" />
                    <span>Mine Refleksjoner</span>
                  </DropdownMenuItem>
                </router-link>
                <DropdownMenuItem @select="authStore.logout" variant="destructive">
                  <LogOut class="h-4 w-4 mr-2" />
                  <span>Logg ut</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </template>
      </div>
    </div>
  </nav>
</template>
