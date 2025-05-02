<script setup lang="ts">
import { ref, computed } from 'vue'
import MapPointTypesManager from '@/components/admin/MapPointTypesManager.vue'
import MapPointsManager from '@/components/admin/MapPointsManager.vue'
import EventsManager from '@/components/admin/EventsManager.vue'
import AdminMapContainer from '@/components/admin/AdminMapContainer.vue'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useGetAllEvents } from '@/api/generated/event/event'
import type { EventResponse as Event, MapPointResponse as MapPoint } from '@/api/generated/model'

// Types
type AdminTab = 'map-point-types' | 'map-points' | 'events'

// Data fetching
const { data: mapPointsData } = useGetAllMapPoints()
const { data: eventsData } = useGetAllEvents()

// Ensure data is arrays
const mapPoints = computed(() => (mapPointsData.value as MapPoint[]) || [])
const events = computed(() => (eventsData.value as Event[]) || [])

// Form states
const activeTab = ref<AdminTab>('map-point-types')
</script>

<template>
  <AdminLayout>
    <div class="flex h-screen">
      <div class="w-128 bg-white border-r border-gray-200 p-4 overflow-y-auto">
        <h2 class="text-2xl font-bold mb-6">Kartstyring</h2>

        <!-- Tab Navigation -->
        <div class="flex space-x-2 mb-6 overflow-scroll">
          <button
            v-for="tab in [
              { id: 'map-point-types', label: 'Kartpunkttyper' },
              { id: 'map-points', label: 'Kartpunkter' },
              { id: 'events', label: 'Hendelser' },
            ]"
            :key="tab.id"
            @click="activeTab = tab.id as AdminTab"
            :class="[
              'px-4 py-2 rounded-lg',
              activeTab === tab.id
                ? 'bg-primary text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200',
            ]"
          >
            {{ tab.label }}
          </button>
        </div>

        <MapPointTypesManager v-if="activeTab === 'map-point-types'" />
        <MapPointsManager v-if="activeTab === 'map-points'" />
        <EventsManager v-if="activeTab === 'events'" />
      </div>

      <AdminMapContainer :map-points="mapPoints" :events="events" />
    </div>
  </AdminLayout>
</template>
