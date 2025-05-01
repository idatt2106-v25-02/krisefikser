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
          <router-link
            to="/husstand"
            v-if="authStore.isAuthenticated"
            class="flex items-center text-gray-700 hover:text-blue-600 transition"
          >
            <Home class="h-5 w-5 mr-1" />
            <span>Husstand</span>
          </router-link>

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

          <!-- Show user profile when authenticated -->
          <div v-else class="flex items-center space-x-2">
            <span class="text-gray-700">{{ authStore.currentUser?.firstName }} {{ authStore.currentUser?.lastName }}</span>
            <DropdownMenu>
              <DropdownMenuTrigger>
                <button class="flex items-center text-gray-700 hover:text-blue-600 transition">
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
        <router-link
          to="/husstand"
          v-if="authStore.isAuthenticated"
          class="flex items-center px-3 py-2 rounded text-gray-700 hover:bg-gray-200"
        >
          <Home class="h-5 w-5 mr-2" />
          <span>Husstand</span>
        </router-link>

        <router-link
          v-if="authStore.isAuthenticated"
          to="/husstand/:id/beredskapslager"
          class="flex items-center px-3 py-2 rounded text-gray-700 hover:bg-gray-200"
        >
          <Package class="h-5 w-5 mr-2" />
          <span>Beredskapslager</span>
        </router-link>

        <!-- Show login button when not authenticated -->
        <router-link v-if="!authStore.isAuthenticated" to="/logg-inn" class="flex items-center px-3 py-2 mt-2 rounded bg-blue-600 text-white hover:bg-blue-700">
          <LogIn class="h-5 w-5 mr-2" />
          <span>Logg inn</span>
        </router-link>

        <router-link
          v-if="authStore.isAuthenticated && authStore.isAdmin"
          to="/admin"
          class="flex items-center text-gray-700 hover:text-blue-600 transition"
        >
          <UserIcon class="h-5 w-5 mr-1" />
          <span>Admin</span>
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
import { Map as MapIcon, Home, Package, Menu as MenuIcon, X, LogIn, User as UserIcon, LogOut } from 'lucide-vue-next';
import { useAuthStore } from '@/stores/useAuthStore';
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
    DropdownMenu,
    DropdownMenuTrigger,
    DropdownMenuContent,
    DropdownMenuItem
  },
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  },
  data() {
    return {
      isMenuOpen: false,
    }
  },
}
</script>
