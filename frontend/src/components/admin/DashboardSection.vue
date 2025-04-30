<!-- DashboardSection.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import {
  Map,
  AlertTriangle,
  BookOpen,
  Trophy,
  Mail,
  ArrowRight
} from 'lucide-vue-next';

// Import shadcn Button component
import { Button } from '@/components/ui/button';

defineEmits(['navigateToMap', 'switchSection']);

// Stats for dashboard - keeping orange for active events, but changing other colors to blue variations
const stats = [
  { title: 'Aktive hendelser', value: 2, icon: AlertTriangle, color: 'text-orange-500', bgColor: 'bg-orange-100', borderColor: 'border-orange-200' },
  { title: 'Kart-elementer', value: 128, icon: Map, color: 'text-blue-500', bgColor: 'bg-blue-100', borderColor: 'border-blue-200' },
  { title: 'Scenarioer', value: 3, icon: BookOpen, color: 'text-blue-600', bgColor: 'bg-blue-100', borderColor: 'border-blue-200' },
  { title: 'Gamification aktiviteter', value: 3, icon: Trophy, color: 'text-blue-700', bgColor: 'bg-blue-100', borderColor: 'border-blue-200' }
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

const getLevelClass = (level: string) => {
  switch(level.toLowerCase()) {
    case 'rød': return 'bg-red-100 text-red-800 border-red-200';
    case 'gul': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    case 'grønn': return 'bg-green-100 text-green-800 border-green-200';
    default: return 'bg-gray-100 text-gray-800 border-gray-200';
  }
};

const getBorderClass = (level: string) => {
  switch(level.toLowerCase()) {
    case 'rød': return 'border-l-4 border-l-red-500';
    case 'gul': return 'border-l-4 border-l-yellow-500';
    case 'grønn': return 'border-l-4 border-l-green-500';
    default: return '';
  }
};
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Dashboard oversikt</h2>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div
        v-for="(stat, index) in stats"
        :key="index"
        class="bg-white rounded-lg shadow p-6 transition-all hover:shadow-md"
        :class="{ 'border-t-4': true, [stat.borderColor]: stat.title === 'Aktive hendelser', 'border-blue-200': stat.title !== 'Aktive hendelser' }"
      >
        <div class="flex items-center">
          <div :class="`${stat.color} ${stat.bgColor} p-3 rounded-full`">
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
    <div class="bg-white rounded-lg shadow mb-6 border-t-2 ">
      <div class="p-4 border-b flex justify-between items-center">
        <h3 class="text-lg font-medium text-gray-800">Nylige hendelser</h3>
        <Button
          @click="$emit('switchSection', 'events')"
          variant="link"
          class="text-blue-600 hover:text-blue-800 text-sm p-0 h-auto flex items-center group"
        >
          Se alle
          <ArrowRight class="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
        </Button>
      </div>
      <div class="p-4">
        <div
          v-for="event in events"
          :key="event.id"
          class="mb-4 last:mb-0 p-3 border rounded-lg hover:shadow-sm transition-shadow"
          :class="getBorderClass(event.level)"
        >
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
    <div class="bg-white rounded-lg shadow border-t-2 ">
      <div class="p-4 border-b">
        <h3 class="text-lg font-medium text-gray-800">Hurtighandlinger</h3>
      </div>
      <div class="p-4 grid grid-cols-1 md:grid-cols-3 gap-4">
        <Button
          @click="$emit('navigateToMap')"
          variant="outline"
          class="flex items-center p-3 justify-start h-auto border-blue-200 hover:bg-blue-50 hover:border-blue-300 transition-colors"
        >
          <div class="bg-blue-100 p-2 rounded-full">
            <Map class="h-5 w-5 text-blue-600" />
          </div>
          <span class="ml-2 text-gray-700">Legg til kartposisjon</span>
        </Button>

        <Button
          @click="$emit('switchSection', 'events')"
          variant="outline"
          class="flex items-center p-3 justify-start h-auto border-orange-200 hover:bg-orange-50 hover:border-orange-300 transition-colors"
        >
          <div class="bg-orange-100 p-2 rounded-full">
            <AlertTriangle class="h-5 w-5 text-orange-600" />
          </div>
          <span class="ml-2 text-gray-700">Registrer hendelse</span>
        </Button>

        <Button
          @click="$emit('switchSection', 'admins')"
          variant="outline"
          class="flex items-center p-3 justify-start h-auto border-blue-200 hover:bg-blue-50 hover:border-blue-300 transition-colors"
        >
          <div class="bg-blue-100 p-2 rounded-full">
            <Mail class="h-5 w-5 text-blue-600" />
          </div>
          <span class="ml-2 text-gray-700">Inviter admin</span>
        </Button>
      </div>
    </div>
  </div>
</template>
