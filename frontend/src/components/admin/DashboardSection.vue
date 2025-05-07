<!-- DashboardSection.vue -->
<script setup lang="ts">
import { computed } from 'vue'
import { Map, AlertTriangle, BookOpen, Trophy, Mail, ArrowRight } from 'lucide-vue-next'

// Import shadcn Button component
import { Button } from '@/components/ui/button';
import { useGetAllEvents } from '@/api/generated/event/event.ts';
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point.ts';
import { useGetAllScenarios } from '@/api/generated/scenario/scenario.ts';
import type { EventResponse as Event, EventResponseLevel as EventLevel, MapPointResponse as MapPoint } from '@/api/generated/model';
import { RouterLink as Link } from 'vue-router';
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'

defineEmits(['navigateToMap'])

// Stats for dashboard - keeping orange for active events, but changing other colors to blue variations
const stats = [
  {
    title: 'Aktive hendelser',
    value: 2,
    icon: AlertTriangle,
    color: 'text-orange-500',
    bgColor: 'bg-orange-100',
    borderColor: 'border-orange-200',
  },
  {
    title: 'Kart-elementer',
    value: 128,
    icon: Map,
    color: 'text-blue-500',
    bgColor: 'bg-blue-100',
    borderColor: 'border-blue-200',
  },
  {
    title: 'Scenarioer',
    value: 3,
    icon: BookOpen,
    color: 'text-blue-600',
    bgColor: 'bg-blue-100',
    borderColor: 'border-blue-200',
  },
  {
    title: 'Gamification aktiviteter',
    value: 3,
    icon: Trophy,
    color: 'text-blue-700',
    bgColor: 'bg-blue-100',
    borderColor: 'border-blue-200',
  },
]

// Fetch events using TanStack Query
const { data: eventsData, isLoading: isLoadingEvents } = useGetAllEvents<Event[]>()

// Fetch map points using TanStack Query
const { data: mapPointsData } = useGetAllMapPoints<MapPoint[]>()

// Fetch scenarios using TanStack Query
const { data: scenariosData } = useGetAllScenarios();

// Computed properties for stats
const activeEventsCount = computed(() => {
  if (!eventsData.value) return 0
  return eventsData.value.filter((event: Event) => event.status === 'ONGOING').length
})

const mapPointsCount = computed(() => {
  if (!mapPointsData.value) return 0
  return mapPointsData.value.length
})

const scenariosCount = computed(() => {
  if (!scenariosData.value) return 0;
  return scenariosData.value.length;
});

// Update stats with real data
const updatedStats = computed(() => [
  { ...stats[0], value: activeEventsCount.value },
  { ...stats[1], value: mapPointsCount.value },
  { ...stats[2], value: scenariosCount.value },
  ...stats.slice(3)
]);

const getLevelClass = (level?: EventLevel) => {
  if (!level) return 'bg-gray-100 text-gray-800 border-gray-200'
  switch (level) {
    case 'RED':
      return 'bg-red-100 text-red-800 border-red-200'
    case 'YELLOW':
      return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    case 'GREEN':
      return 'bg-green-100 text-green-800 border-green-200'
    default:
      return 'bg-gray-100 text-gray-800 border-gray-200'
  }
}

const getBorderClass = (level?: EventLevel) => {
  if (!level) return ''
  switch (level) {
    case 'RED':
      return 'border-l-4 border-l-red-500'
    case 'YELLOW':
      return 'border-l-4 border-l-yellow-500'
    case 'GREEN':
      return 'border-l-4 border-l-green-500'
    default:
      return ''
  }
}

const formatLocation = (event: Event) => {
  if (event.latitude && event.longitude) {
    return `${event.latitude.toFixed(4)}, ${event.longitude.toFixed(4)}`
  }
  return 'Ukjent lokasjon'
}

const authStore = useAuthStore()
</script>

<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">Dashboard oversikt</h2>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div
        v-for="(stat, index) in updatedStats"
        :key="index"
        class="bg-white rounded-lg shadow p-6 transition-all hover:shadow-md"
        :class="{
          'border-t-4': true,
          [stat.borderColor]: stat.title === 'Aktive hendelser',
          'border-blue-200': stat.title !== 'Aktive hendelser',
        }"
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
    <div class="bg-white rounded-lg shadow mb-6 border-t-2">
      <div class="p-4 border-b flex justify-between items-center">
        <h3 class="text-lg font-medium text-gray-800">Nylige hendelser</h3>
        <Link to="/admin/kart">
          <Button
            variant="link"
            class="text-blue-600 hover:text-blue-800 text-sm p-0 h-auto flex items-center group"
          >
            Se alle
            <ArrowRight class="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform" />
          </Button>
        </Link>
      </div>
      <div class="p-4">
        <div v-if="isLoadingEvents" class="text-center py-4">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
        </div>
        <div v-else-if="eventsData && eventsData.length > 0">
          <div
            v-for="event in eventsData.slice(0, 5)"
            :key="event.id"
            class="mb-4 last:mb-0 p-3 border rounded-lg hover:shadow-sm transition-shadow"
            :class="getBorderClass(event.level)"
          >
            <div class="flex justify-between">
              <h4 class="font-medium">{{ event.title || 'Uten tittel' }}</h4>
              <span
                :class="`px-2 py-1 rounded-full text-xs font-medium ${getLevelClass(event.level)}`"
              >
                {{ event.level || 'UKJENT' }}
              </span>
            </div>
            <p class="text-gray-600 text-sm mt-1">{{ event.description || 'Ingen beskrivelse' }}</p>
            <div class="flex justify-between mt-2 text-xs text-gray-500">
              <span>{{ formatLocation(event) }}</span>
              <span>{{
                event.startTime
                  ? new Date(event.startTime).toLocaleString('no-NO')
                  : 'Ingen starttid'
              }}</span>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-4 text-gray-500">Ingen hendelser funnet</div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white rounded-lg shadow border-t-2">
      <div class="p-4 border-b">
        <h3 class="text-lg font-medium text-gray-800">Hurtighandlinger</h3>
      </div>
      <div class="p-4 grid grid-cols-1 md:grid-cols-3 gap-4">
        <Button
          as-child
          variant="outline"
          class="flex items-center p-3 justify-start h-auto border-blue-200 hover:bg-blue-50 hover:border-blue-300 transition-colors"
        >
          <Link to="/admin/kart">
            <div class="bg-blue-100 p-2 rounded-full">
              <Map class="h-5 w-5 text-blue-600" />
            </div>
            <span class="ml-2 text-gray-700">Legg til kartposisjon</span>
          </Link>
        </Button>
        <Button
          as-child
          variant="outline"
          class="flex items-center p-3 justify-start h-auto border-orange-200 hover:bg-orange-50 hover:border-orange-300 transition-colors"
        >
          <Link to="/admin/kart" class="flex items-center w-full">
            <div class="bg-orange-100 p-2 rounded-full">
              <AlertTriangle class="h-5 w-5 text-orange-600" />
            </div>
            <span class="ml-2 text-gray-700">Registrer hendelse</span>
          </Link>
        </Button>

        <Button
          as-child
          v-if="authStore.isSuperAdmin"
          variant="outline"
          class="flex items-center p-3 justify-start h-auto border-blue-200 hover:bg-blue-50 hover:border-blue-300 transition-colors"
        >
          <Link to="/super-admin/behandle-administratorer" class="flex items-center w-full">
            <div class="bg-blue-100 p-2 rounded-full">
              <Mail class="h-5 w-5 text-blue-600" />
            </div>
            <span class="ml-2 text-gray-700">Inviter admin</span>
          </Link>
        </Button>
      </div>
    </div>
  </div>
</template>
