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
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

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
  },
  setup() {
    const authStore = useAuthStore()
    const route = useRoute()

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
        label: 'Husstand',
        to: authStore.isAuthenticated ? '/husstand' : '/bli-med-eller-opprett-husstand',
        icon: Home,
        show: true,
      },
      {
        label: 'Beredskapslager',
        to: '/husstand/:id/beredskapslager',
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

    return { authStore, filteredNavItems, isActive }
  },
  data() {
    return {
      isMenuOpen: false,
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
            v-for="item in filteredNavItems"
            :key="item.label"
            :to="item.to"
            :class="[
              'flex items-center transition',
              isActive(item.to) ? 'text-blue-600 font-medium' : 'text-gray-700 hover:text-blue-600',
            ]"
          >
            <component :is="item.icon" class="h-5 w-5 mr-1" />
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
              <DropdownMenu>
                <DropdownMenuTrigger>
                  <button
                    class="flex items-center text-gray-700 hover:text-blue-600 transition"
                    aria-label="Min profil"
                  >
                    <span>
                      {{ authStore.currentUser?.firstName }}
                      {{ authStore.currentUser?.lastName }}
                    </span>
                    <UserIcon class="h-5 w-5 ml-2" />
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
              ? 'text-blue-600 font-medium bg-blue-50'
              : 'text-gray-700 hover:bg-gray-200',
          ]"
        >
          <component :is="item.icon" class="h-5 w-5 mr-2" />
          <span>{{ item.label }}</span>
        </router-link>

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
                  class="flex items-center text-gray-700 hover:text-blue-600"
                  aria-label="Min profil"
                >
                  <UserIcon class="h-5 w-5 mr-1" />
                  <span>
                    {{ authStore.currentUser?.firstName }}
                    {{ authStore.currentUser?.lastName }}
                  </span>
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
        </template>
      </div>
    </div>
  </nav>
</template>
