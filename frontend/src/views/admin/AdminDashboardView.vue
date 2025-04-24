<!-- AdminDashboard.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  Map,
  AlertTriangle,
  BookOpen,
  Trophy,
  Users,
  ShieldCheck
} from 'lucide-vue-next';

// Import our new components
import DashboardSection from '@/components/admin/DashboardSection.vue';
import EventsSection from '@/components/admin/EventSection.vue';
import ScenariosSection from '@/components/admin/ScenariosSection.vue';
import GamificationSection from '@/components/admin/GamificationSection.vue';
import AdminsSection from '@/components/admin/AdminSection.vue';

const router = useRouter();
const activeSection = ref('dashboard');

// Set user role - in a real app, this would come from auth system
// Options: 'Super Admin' or 'Admin'
const userRole = ref('Super Admin');

// Computed property to check if user is super admin
const isSuperAdmin = computed(() => userRole.value === 'Super Admin');

// Function to toggle between admin types (for demonstration)
const toggleAdminRole = () => {
  userRole.value = userRole.value === 'Super Admin' ? 'Admin' : 'Super Admin';
};

const navigateToMap = () => {
  router.push('/admin/kart');
};
</script>

<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Sidebar -->
    <div class="w-64 bg-white shadow-md">
      <div class="p-4 border-b">
        <h1 class="text-xl font-bold text-blue-600">Admin Dashboard</h1>
      </div>
      <nav class="mt-4">
        <button
          @click="activeSection = 'dashboard'"
          :class="['flex items-center w-full px-4 py-3 text-left hover:bg-blue-50',
                  activeSection === 'dashboard' ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600' : 'text-gray-700']"
        >
          <span class="flex-shrink-0 w-6">
            <i class="fas fa-home"></i>
          </span>
          <span class="ml-2">Dashboard</span>
        </button>

        <button
          @click="navigateToMap()"
          class="flex items-center w-full px-4 py-3 text-left text-gray-700 hover:bg-blue-50"
        >
          <Map class="h-5 w-5" />
          <span class="ml-2">Kart administrasjon</span>
        </button>

        <button
          @click="activeSection = 'events'"
          :class="['flex items-center w-full px-4 py-3 text-left hover:bg-blue-50',
                  activeSection === 'events' ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600' : 'text-gray-700']"
        >
          <AlertTriangle class="h-5 w-5" />
          <span class="ml-2">Hendelser</span>
        </button>

        <button
          @click="activeSection = 'scenarios'"
          :class="['flex items-center w-full px-4 py-3 text-left hover:bg-blue-50',
                  activeSection === 'scenarios' ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600' : 'text-gray-700']"
        >
          <BookOpen class="h-5 w-5" />
          <span class="ml-2">Scenarioer</span>
        </button>

        <button
          @click="activeSection = 'gamification'"
          :class="['flex items-center w-full px-4 py-3 text-left hover:bg-blue-50',
                  activeSection === 'gamification' ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600' : 'text-gray-700']"
        >
          <Trophy class="h-5 w-5" />
          <span class="ml-2">Gamification</span>
        </button>

        <button
          @click="activeSection = 'admins'"
          :class="['flex items-center w-full px-4 py-3 text-left hover:bg-blue-50',
                  activeSection === 'admins' ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600' : 'text-gray-700']"
        >
          <Users class="h-5 w-5" />
          <span class="ml-2">Admin brukere</span>
        </button>
      </nav>
    </div>

    <!-- Main Content -->
    <div class="flex-1 overflow-auto">
      <!-- Top bar -->
      <div class="bg-white p-4 shadow flex justify-between items-center">
        <div class="flex items-center">
          <h2 class="text-lg font-medium text-gray-800"></h2>
        </div>

        <!-- Admin role indicator with toggle capability (for demo only) -->
        <button @click="toggleAdminRole" class="flex items-center p-1.5 hover:bg-gray-100 rounded-lg transition-colors">
          <div class="h-8 w-8 rounded-full bg-blue-500 flex items-center justify-center text-white font-medium">
            <ShieldCheck v-if="isSuperAdmin" class="h-4 w-4" />
            <span v-else>A</span>
          </div>
          <div class="ml-2 text-left">
            <span class="text-gray-700 font-medium block">{{ userRole }}</span>
            <span class="text-xs text-gray-500">Klikk for å bytte rolle (demo)</span>
          </div>
        </button>
      </div>

      <!-- Dynamic section content -->
      <DashboardSection
        v-if="activeSection === 'dashboard'"
        @navigate-to-map="navigateToMap"
        @switch-section="activeSection = $event"
      />

      <EventsSection
        v-if="activeSection === 'events'"
      />

      <ScenariosSection
        v-if="activeSection === 'scenarios'"
      />

      <GamificationSection
        v-if="activeSection === 'gamification'"
      />

      <AdminsSection
        v-if="activeSection === 'admins'"
        :is-super-admin="isSuperAdmin"
        @navigate="router.push($event)"
      />
    </div>
  </div>
</template>
