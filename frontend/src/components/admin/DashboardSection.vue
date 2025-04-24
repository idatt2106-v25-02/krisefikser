<!-- DashboardSection.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import {
  Map,
  AlertTriangle,
  BookOpen,
  Trophy,
  Mail
} from 'lucide-vue-next';

defineEmits(['navigateToMap', 'switchSection']);

// Stats for dashboard
const stats = [
  { title: 'Aktive hendelser', value: 2, icon: AlertTriangle, color: 'text-orange-500' },
  { title: 'Kart-elementer', value: 128, icon: Map, color: 'text-blue-500' },
  { title: 'Scenarioer', value: 3, icon: BookOpen, color: 'text-green-500' },
  { title: 'Gamification aktiviteter', value: 3, icon: Trophy, color: 'text-purple-500' }
];

// Mock data for events
const events = ref([
  {
    id: '1',
    title: 'Flom i Trondheim',
    description: 'Økt vannstand i Nidelva',
    level: 'rød',
    location: 'Trondheim sentrum',
    timestamp: '2025-04-22T10:30:00',
    active: true
  },
  {
    id: '2',
    title: 'Strømbrudd',
    description: 'Midlertidig strømbrudd grunnet vedlikehold',
    level: 'gul',
    location: 'Oslo vest',
    timestamp: '2025-04-23T08:00:00',
    active: true
  }
]);

const getLevelClass = (level) => {
  switch(level.toLowerCase()) {
    case 'rød': return 'bg-red-100 text-red-800 border-red-200';
    case 'gul': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    case 'grønn': return 'bg-green-100 text-green-800 border-green-200';
    default: return 'bg-gray-100 text-gray-800 border-gray-200';
  }
};
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Dashboard oversikt</h2>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div v-for="(stat, index) in stats" :key="index" class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div :class="`${stat.color} bg-opacity-10 p-3 rounded-full`">
            <component :is="stat.icon" class="h-6 w-6" />
          </div>
          <div class="ml-4">
            <h3 class="text-gray-500 text-sm">{{ stat.title }}</h3>
            <p class="text-2xl font-bold text-gray-800">{{ stat.value }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Events -->
    <div class="bg-white rounded-lg shadow mb-6">
      <div class="p-4 border-b flex justify-between items-center">
        <h3 class="text-lg font-medium text-gray-800">Nylige hendelser</h3>
        <button @click="$emit('switchSection', 'events')" class="text-blue-600 hover:text-blue-800 text-sm">Se alle</button>
      </div>
      <div class="p-4">
        <div v-for="event in events" :key="event.id" class="mb-4 last:mb-0 p-3 border rounded-lg">
          <div class="flex justify-between">
            <h4 class="font-medium">{{ event.title }}</h4>
            <span :class="`px-2 py-1 rounded-full text-xs font-medium ${getLevelClass(event.level)}`">
              {{ event.level.toUpperCase() }}
            </span>
          </div>
          <p class="text-gray-600 text-sm mt-1">{{ event.description }}</p>
          <div class="flex justify-between mt-2 text-xs text-gray-500">
            <span>{{ event.location }}</span>
            <span>{{ new Date(event.timestamp).toLocaleString('no-NO') }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white rounded-lg shadow">
      <div class="p-4 border-b">
        <h3 class="text-lg font-medium text-gray-800">Hurtighandlinger</h3>
      </div>
      <div class="p-4 grid grid-cols-1 md:grid-cols-3 gap-4">
        <button @click="$emit('navigateToMap')" class="flex items-center p-3 border rounded-lg hover:bg-blue-50 transition-colors">
          <Map class="h-5 w-5 text-blue-600" />
          <span class="ml-2 text-gray-700">Legg til kartposisjon</span>
        </button>
        <button @click="$emit('switchSection', 'events')" class="flex items-center p-3 border rounded-lg hover:bg-blue-50 transition-colors">
          <AlertTriangle class="h-5 w-5 text-orange-600" />
          <span class="ml-2 text-gray-700">Registrer hendelse</span>
        </button>
        <button @click="$emit('switchSection', 'admins')" class="flex items-center p-3 border rounded-lg hover:bg-blue-50 transition-colors">
          <Mail class="h-5 w-5 text-green-600" />
          <span class="ml-2 text-gray-700">Inviter admin</span>
        </button>
      </div>
    </div>
  </div>
</template>
