<script setup lang="ts">
import { ref } from 'vue'
import MapPointTypesManager from '@/components/admin/map/MapPointTypesManager.vue'
import MapPointsManager from '@/components/admin/map/MapPointsManager.vue'
import EventsManager from '@/components/admin/event/EventsManager.vue'
import AdminMapContainer from '@/components/admin/map/AdminMapContainer.vue'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useGetAllEvents } from '@/api/generated/event/event'

type AdminTab = 'map-point-types' | 'map-points' | 'events'

// Data fetching
const { data: mapPoints } = useGetAllMapPoints()
const { data: events } = useGetAllEvents()

// Form states
const activeTab = ref<AdminTab>('map-point-types')
const selectMode = ref(false)
const mapCoordinates = ref<{ lat: number; lng: number } | null>(null)

const handleMapClick = (lat: number, lng: number) => {
  if (selectMode.value) {
    mapCoordinates.value = { lat, lng }
  }
}

const handleSelectModeChange = (isActive: boolean) => {
  selectMode.value = isActive
}

// Watch for coordinate changes to reset after they've been used
const onMapCoordinatesEmitted = () => {
  mapCoordinates.value = null
}
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
            :class="[
              'px-4 py-2 rounded-lg',
              activeTab === tab.id
                ? 'bg-primary text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200',
            ]"
            @click="activeTab = tab.id as AdminTab"
          >
            {{ tab.label }}
          </button>
        </div>

        <MapPointTypesManager v-if="activeTab === 'map-point-types'" />
        <MapPointsManager
          v-if="activeTab === 'map-points'"
          :map-coordinates="mapCoordinates"
          @map-selection-mode-change="handleSelectModeChange"
          @map-coordinates-handled="onMapCoordinatesEmitted"
        />
        <EventsManager
          v-if="activeTab === 'events'"
          :map-coordinates="mapCoordinates"
          @map-selection-mode-change="handleSelectModeChange"
          @map-coordinates-handled="onMapCoordinatesEmitted"
        />
      </div>

      <AdminMapContainer
        :events="events"
        :map-points="mapPoints"
        :select-mode="selectMode"
        @map-click="handleMapClick"
      />
    </div>
  </AdminLayout>
</template>
