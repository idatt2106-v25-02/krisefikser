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
          <router-link v-if="!authStore.isAuthenticated" to="/logg-inn" class="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition ml-2">
            Logg inn
          </router-link>

          <!-- Show notifications and user profile when authenticated -->
          <div v-else class="flex items-center space-x-4">
            <!-- Notifications Bell -->
            <DropdownMenu>
              <DropdownMenuTrigger>
                <button class="flex items-center text-gray-700 hover:text-blue-600 transition relative" aria-label="Varsler">
                  <BellIcon class="h-5 w-5" aria-label="Varsler" />
                  <span v-if="unreadCountData && unreadCountData > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
                    {{ unreadCountData > 9 ? '9+' : unreadCountData }}
                  </span>
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent class="w-80">
                <div class="p-2 border-b border-gray-100 flex justify-between items-center">
                  <h3 class="font-medium text-gray-900">Varsler</h3>
                  <button @click="() => refetchNotifications()" :disabled="isFetchingNotifications" class="text-blue-600 hover:text-blue-800 disabled:opacity-50">
                    <RefreshCw class="h-4 w-4" :class="{'animate-spin': isFetchingNotifications}"/>
                  </button>
                </div>
                <div class="max-h-96 overflow-y-auto">
                  <div v-if="isLoadingNotifications" class="p-4 text-center text-gray-500">
                    Laster...
                  </div>
                  <div v-else-if="notificationsError" class="p-4 text-center text-red-500">
                    Feil ved lasting
                  </div>
                  <div v-else-if="!notifications || notifications.length === 0" class="p-4 text-center text-gray-500">
                    Ingen nye varsler
                  </div>
                  <DropdownMenuItem v-for="notification in notifications" :key="notification.id" class="cursor-pointer p-0">
                    <div
                      @click="handleNotificationClick(notification)"
                      class="p-3 hover:bg-blue-50 border-b border-gray-100 w-full"
                      :class="{ 'bg-blue-50': !notification.read }"
                    >
                      <div class="flex items-start gap-2">
                        <div
                          class="rounded-full p-2 flex-shrink-0"
                          :class="{
                            'bg-yellow-100 text-yellow-600': (notification.type as string) === 'EXPIRY',
                            'bg-red-100 text-red-600': (notification.type as string) === 'CRISIS',
                            'bg-blue-100 text-blue-600': (notification.type as string) === 'UPDATE'
                          }"
                        >
                          <AlertTriangle v-if="(notification.type as string) === 'CRISIS'" class="h-4 w-4" />
                          <Calendar v-else-if="(notification.type as string) === 'EXPIRY'" class="h-4 w-4" />
                          <Bell v-else-if="(notification.type as string) === 'UPDATE'" class="h-4 w-4" />
                          <Info v-else class="h-4 w-4" />
                        </div>
                        <div>
                          <p class="text-sm font-medium text-gray-900">{{ notification.title || '-' }}</p>
                          <p class="text-xs text-gray-500 mt-1">{{ notification.message || '-' }}</p>
                          <p class="text-xs text-gray-400 mt-1">{{ notification.createdAt ? formatDate(notification.createdAt) : '-' }}</p>
                        </div>
                      </div>
                    </div>
                  </DropdownMenuItem>
                </div>
                <div v-if="notifications && notifications.length > 0" class="p-2 border-t border-gray-100 space-y-2">
                  <button
                    @click="markAllAsRead"
                    :disabled="isMarkingAllAsRead"
                    class="text-sm text-blue-600 hover:text-blue-800 w-full text-center disabled:opacity-50"
                  >
                    {{ isMarkingAllAsRead ? 'Markerer...' : 'Marker alle som lest' }}
                  </button>
                </div>
                <div class="p-2 border-t border-gray-100">
                  <router-link to="/varsler" class="text-sm text-blue-600 hover:text-blue-800 flex items-center justify-center">
                    <span>Se alle varsler</span>
                    <ArrowRight class="h-3 w-3 ml-1" />
                  </router-link>
                </div>
              </DropdownMenuContent>
            </DropdownMenu>

            <span class="text-gray-700">{{ authStore.currentUser?.firstName }} {{ authStore.currentUser?.lastName }}</span>
            <DropdownMenu>
              <DropdownMenuTrigger>
                <button class="flex items-center text-gray-700 hover:text-blue-600 transition" aria-label="Min profil">
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
          <span v-if="unreadCountData && unreadCountData > 0" class="bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
            {{ unreadCountData > 9 ? '9+' : unreadCountData }}
          </span>
        </div>

        <!-- Mobile notifications panel -->
        <div v-if="showMobileNotifications && authStore.isAuthenticated" class="mt-2 bg-white rounded-md shadow-lg p-2">
          <div class="p-2 border-b border-gray-100 flex justify-between items-center">
            <h3 class="font-medium text-gray-900">Varsler</h3>
            <button @click="() => refetchNotifications()" :disabled="isFetchingNotifications" class="text-blue-600 hover:text-blue-800 disabled:opacity-50">
              <RefreshCw class="h-4 w-4" :class="{'animate-spin': isFetchingNotifications}"/>
            </button>
          </div>
          <div class="max-h-96 overflow-y-auto">
            <div v-if="isLoadingNotifications" class="p-4 text-center text-gray-500">
              Laster...
            </div>
            <div v-else-if="notificationsError" class="p-4 text-center text-red-500">
              Feil ved lasting
            </div>
            <div v-else-if="!notifications || notifications.length === 0" class="p-4 text-center text-gray-500">
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
                    'bg-blue-100 text-blue-600': (notification.type as string) === 'UPDATE'
                  }"
                >
                  <AlertTriangle v-if="(notification.type as string) === 'CRISIS'" class="h-4 w-4" />
                  <Calendar v-else-if="(notification.type as string) === 'EXPIRY'" class="h-4 w-4" />
                  <Bell v-else-if="(notification.type as string) === 'UPDATE'" class="h-4 w-4" />
                  <Info v-else class="h-4 w-4" />
                </div>
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ notification.title || '-' }}</p>
                  <p class="text-xs text-gray-500 mt-1">{{ notification.message || '-' }}</p>
                  <p class="text-xs text-gray-400 mt-1">{{ notification.createdAt ? formatDate(notification.createdAt) : '-' }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-if="notifications && notifications.length > 0" class="p-2 border-t border-gray-100">
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
<script lang="ts">
import { Map as MapIcon, Home, Package, Menu as MenuIcon, X, LogIn, User as UserIcon, LogOut, Bell as BellIcon, AlertTriangle, Calendar, Bell, Info, RefreshCw, ArrowRight } from 'lucide-vue-next';
import { useAuthStore } from '@/stores/useAuthStore';
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem
} from '@/components/ui/dropdown-menu';
import {
  useGetNotifications,
  useGetUnreadCount,
  useReadNotification,
  useReadAll
} from '@/api/generated/notification/notification';
import { useQueryClient } from '@tanstack/vue-query';
import type { NotificationResponse, GetNotificationsParams } from '@/api/generated/model';
import type { ErrorType } from '@/api/axios';
import type { UseMutationOptions } from '@tanstack/vue-query';

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
    DropdownMenuItem
  },
  setup() {
    const authStore = useAuthStore();
    const router = useRouter();
    const isMenuOpen = ref(false);
    const showMobileNotifications = ref(false);

    const queryClient = useQueryClient();

    const notificationParams = computed<GetNotificationsParams>(() => ({
      pageable: {
        page: 0,
        size: 5,
        sort: ['createdAt,desc']
      }
    }));

    const {
      data: notificationsData,
      isLoading: isLoadingNotifications,
      isFetching: isFetchingNotifications,
      error: notificationsError,
      refetch: refetchNotifications
    } = useGetNotifications(notificationParams, {
      query: {
        enabled: computed(() => authStore.isAuthenticated),
        staleTime: 1000 * 60 * 5
      }
    });

    const {
      data: unreadCountData,
      refetch: refetchUnreadCount
    } = useGetUnreadCount({
      query: {
        enabled: computed(() => authStore.isAuthenticated),
        staleTime: 1000 * 60 * 5
      }
    });

    const { mutate: mutateReadNotification, isPending: _isMarkingAsRead } = useReadNotification({
      mutation: {
        onSuccess: () => {
          queryClient.invalidateQueries({ queryKey: ['/api/notifications'] });
          queryClient.invalidateQueries({ queryKey: ['/api/notifications/unread'] });
        },
        onError: (error: ErrorType<unknown>) => {
          console.error("Failed to mark notification as read:", error);
        }
      }
    });

    const { mutate: mutateReadAll, isPending: isMarkingAllAsRead } = useReadAll({
      mutation: {
        onSuccess: () => {
          queryClient.invalidateQueries({ queryKey: ['/api/notifications'] });
          queryClient.invalidateQueries({ queryKey: ['/api/notifications/unread'] });
        },
        onError: (error: ErrorType<unknown>) => {
          console.error("Failed to mark all notifications as read:", error);
        }
      }
    });

    const notifications = computed(() => notificationsData.value?.content || []);

    const handleNotificationClick = (notification: NotificationResponse) => {
      if (notification.id && !notification.read) {
        mutateReadNotification({ id: notification.id });
      }

      if (notification.url) {
        window.open(notification.url, '_blank');
      }

      showMobileNotifications.value = false;
    };

    const markAllAsRead = () => {
      mutateReadAll();
    };

    const formatDate = (dateString: string) => {
      if (!dateString) return '-';
      const now = new Date();
      const notificationDate = new Date(dateString);
      const diffInDays = Math.floor((now.getTime() - notificationDate.getTime()) / (1000 * 60 * 60 * 24));
      if (diffInDays === 0) return `I dag, ${notificationDate.getHours().toString().padStart(2, '0')}:${notificationDate.getMinutes().toString().padStart(2, '0')}`;
      if (diffInDays === 1) return 'I går';
      if (diffInDays < 7) {
        const days = ['Søndag', 'Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag'];
        return days[notificationDate.getDay()];
      }
      return `${notificationDate.getDate().toString().padStart(2, '0')}.${(notificationDate.getMonth() + 1).toString().padStart(2, '0')}.${notificationDate.getFullYear()}`;
    };

    watch(() => authStore.isAuthenticated, (isAuth) => {
      if (isAuth) {
        refetchNotifications();
        refetchUnreadCount();
      }
    });

    return {
      authStore,
      isMenuOpen,
      isLoadingNotifications,
      isFetchingNotifications,
      notificationsError,
      notifications,
      unreadCountData,
      handleNotificationClick,
      markAllAsRead,
      isMarkingAllAsRead,
      formatDate,
      showMobileNotifications,
      refetchNotifications
    };
  },
}
</script>
