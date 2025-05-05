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
                  <span v-if="notifications.length > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
                    {{ notifications.length > 9 ? '9+' : notifications.length }}
                  </span>
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent class="w-80">
                <div class="p-2 border-b border-gray-100">
                  <h3 class="font-medium text-gray-900">Varsler</h3>
                </div>
                <div class="max-h-96 overflow-y-auto">
                  <div v-if="notifications.length === 0" class="p-4 text-center text-gray-500">
                    Ingen nye varsler
                  </div>
                  <DropdownMenuItem v-for="(notification, index) in notifications" :key="index" class="cursor-pointer p-0">
                    <div
                      @click="handleNotificationClick(notification)"
                      class="p-3 hover:bg-blue-50 border-b border-gray-100 w-full"
                      :class="{ 'bg-blue-50': !notification.read }"
                    >
                      <div class="flex items-start gap-2">
                        <div
                          class="rounded-full p-2 flex-shrink-0"
                          :class="{
                            'bg-yellow-100 text-yellow-600': notification.type === 'expiry',
                            'bg-red-100 text-red-600': notification.type === 'crisis',
                            'bg-blue-100 text-blue-600': notification.type === 'update'
                          }"
                        >
                          <AlertTriangle v-if="notification.type === 'crisis'" class="h-4 w-4" />
                          <Calendar v-else-if="notification.type === 'expiry'" class="h-4 w-4" />
                          <Bell v-else class="h-4 w-4" />
                        </div>
                        <div>
                          <p class="text-sm font-medium text-gray-900">{{ notification.title }}</p>
                          <p class="text-xs text-gray-500 mt-1">{{ notification.message }}</p>
                          <p class="text-xs text-gray-400 mt-1">{{ formatDate(notification.createdAt) }}</p>
                        </div>
                      </div>
                    </div>
                  </DropdownMenuItem>
                </div>
                <div v-if="notifications.length > 0" class="p-2 border-t border-gray-100">
                  <button @click="markAllAsRead" class="text-sm text-blue-600 hover:text-blue-800 w-full text-center">
                    Marker alle som lest
                  </button>
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
          <span v-if="notifications.length > 0" class="bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
            {{ notifications.length > 9 ? '9+' : notifications.length }}
          </span>
        </div>

        <!-- Mobile notifications panel -->
        <div v-if="showMobileNotifications && authStore.isAuthenticated" class="mt-2 bg-white rounded-md shadow-lg p-2">
          <div class="p-2 border-b border-gray-100">
            <h3 class="font-medium text-gray-900">Varsler</h3>
          </div>
          <div class="max-h-96 overflow-y-auto">
            <div v-if="notifications.length === 0" class="p-4 text-center text-gray-500">
              Ingen nye varsler
            </div>
            <div
              v-for="(notification, index) in notifications"
              :key="index"
              @click="handleNotificationClick(notification)"
              class="p-3 hover:bg-blue-50 border-b border-gray-100 cursor-pointer"
              :class="{ 'bg-blue-50': !notification.read }"
            >
              <div class="flex items-start gap-2">
                <div
                  class="rounded-full p-2 flex-shrink-0"
                  :class="{
                    'bg-yellow-100 text-yellow-600': notification.type === 'expiry',
                    'bg-red-100 text-red-600': notification.type === 'crisis',
                    'bg-blue-100 text-blue-600': notification.type === 'update'
                  }"
                >
                  <AlertTriangle v-if="notification.type === 'crisis'" class="h-4 w-4" />
                  <Calendar v-else-if="notification.type === 'expiry'" class="h-4 w-4" />
                  <Bell v-else class="h-4 w-4" />
                </div>
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ notification.title }}</p>
                  <p class="text-xs text-gray-500 mt-1">{{ notification.message }}</p>
                  <p class="text-xs text-gray-400 mt-1">{{ formatDate(notification.createdAt) }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-if="notifications.length > 0" class="p-2 border-t border-gray-100">
            <button @click="markAllAsRead" class="text-sm text-blue-600 hover:text-blue-800 w-full text-center">
              Marker alle som lest
            </button>
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
import { Map as MapIcon, Home, Package, Menu as MenuIcon, X, LogIn, User as UserIcon, LogOut, Bell as BellIcon, AlertTriangle, Calendar } from 'lucide-vue-next';
import { useAuthStore } from '@/stores/useAuthStore';
import { useNotificationStore } from '@/stores/notification/useNotificationStore';
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem
} from '@/components/ui/dropdown-menu';

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
    DropdownMenu,
    DropdownMenuTrigger,
    DropdownMenuContent,
    DropdownMenuItem
  },
  setup() {
    const authStore = useAuthStore();
    const notificationStore = useNotificationStore();
    const router = useRouter();
    const isMenuOpen = ref(false);
    const showMobileNotifications = ref(false);

    const notifications = computed(() => {
      return notificationStore.notifications;
    });

    const handleNotificationClick = (notification) => {
      notificationStore.markAsRead(notification.id);

      // Route based on notification type
      if (notification.type === 'crisis') {
        router.push(`/kart?crisis=${notification.referenceId}`);
      } else if (notification.type === 'expiry') {
        router.push(`/husstand/${notification.householdId}/beredskapslager`);
      } else if (notification.type === 'update') {
        router.push(`/kart?update=${notification.referenceId}`);
      }

      // Close mobile notifications panel if open
      showMobileNotifications.value = false;
    };

    const markAllAsRead = () => {
      notificationStore.markAllAsRead();
    };

    const formatDate = (date) => {
      const now = new Date();
      const notificationDate = new Date(date);

      // Calculate the difference in days
      const diffInDays = Math.floor((now - notificationDate) / (1000 * 60 * 60 * 24));

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
      if (authStore.isAuthenticated) {
        notificationStore.fetchNotifications();
      }
    });

    return {
      authStore,
      isMenuOpen,
      notifications,
      handleNotificationClick,
      markAllAsRead,
      formatDate,
      showMobileNotifications
    };
  },
}
</script>
