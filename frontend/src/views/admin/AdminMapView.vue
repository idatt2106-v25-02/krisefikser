<script setup lang="ts">
import { ref } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import ShelterLayer from '@/components/map/ShelterLayer.vue'
import EventLayer from '@/components/map/EventLayer.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import { type Shelter } from '@/components/map/mapData'
import {
  EventResponseLevel as EventLevel,
  EventResponseStatus as EventStatus,
} from '@/api/generated/model'
import L from 'leaflet'
import { useCreateMapPoint, useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { ref, computed } from 'vue'
import MapPointTypesManager from '@/components/admin/MapPointTypesManager.vue'
import MapPointsManager from '@/components/admin/MapPointsManager.vue'
import EventsManager from '@/components/admin/EventsManager.vue'
import AdminMapContainer from '@/components/admin/AdminMapContainer.vue'
import AdminLayout from '@/components/admin/AdminLayout.vue'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useGetAllEvents } from '@/api/generated/event/event'

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
const activeTab = ref<'shelters' | 'events'>('shelters')
const newShelter = ref<Partial<Shelter>>({
  name: '',
  capacity: 0,
  position: [63.4305, 10.3951], // Default to Trondheim center
})
const newEvent = ref({
  title: '',
  description: '',
  radius: 500,
  latitude: 63.4305,
  longitude: 10.3951,
  level: EventLevel.GREEN,
  startTime: new Date().toISOString(),
  endTime: undefined,
  status: EventStatus.UPCOMING,
})

// API mutations
const { mutate: createMapPoint } = useCreateMapPoint()
const { mutate: createEvent } = useCreateEvent()
const { mutate: createMapPointType } = useCreateMapPointType()

// Handle map instance being set
function onMapCreated(map: L.Map) {
  mapInstance.value = map

  // Add click handler for location selection
  map.on('click', (e) => {
    const { lat, lng } = e.latlng
    if (activeTab.value === 'shelters') {
      newShelter.value.position = [lat, lng]
    } else {
      newEvent.value.latitude = lat
      newEvent.value.longitude = lng
    }
  })
}

// Form submission handlers
async function handleAddShelter() {
  if (!authStore.isAuthenticated) {
    console.error('User must be authenticated to create a shelter')
    return
  }

  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to create a shelter')
    return
  }

  if (!newShelter.value.name || !newShelter.value.position) {
    console.error('Missing required shelter fields')
    return
  }

  try {
    // First create the map point type for the shelter
    createMapPointType(
      {
        data: {
          title: newShelter.value.name,
          description: `Shelter with capacity of ${newShelter.value.capacity} people`,
          iconUrl: '/shelter-icon.png',
          openingTime: '24/7',
        },
      },
      {
        onSuccess: (data) => {
          if (!newShelter.value.position) {
            console.error('Shelter position is not set')
            return
          }

          createMapPoint({
            data: {
              latitude: newShelter.value.position[0],
              longitude: newShelter.value.position[1],
              typeId: data.id,
            },
          })
        },
      },
    )

    refetchMapPoints()

    // Reset form
    newShelter.value = {
      name: '',
      capacity: 0,
      position: [63.4305, 10.3951],
    }
  } catch (error) {
    console.error('Error creating shelter:', error)
    if (error && typeof error === 'object' && 'response' in error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiError = error as { response: { data: any; status: number } }
      console.error('Server response:', apiError.response.data)
      console.error('Status code:', apiError.response.status)
    }
  }
}

async function handleAddEvent() {
  if (!authStore.isAuthenticated) {
    console.error('User must be authenticated to create an event')
    return
  }

  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to create an event')
    return
  }

  if (!newEvent.value.title || !newEvent.value.latitude || !newEvent.value.longitude) {
    console.error('Missing required event fields')
    return
  }

  try {
    await createEvent({
      data: {
        title: newEvent.value.title,
        description: newEvent.value.description,
        radius: newEvent.value.radius,
        latitude: newEvent.value.latitude,
        longitude: newEvent.value.longitude,
        level: newEvent.value.level,
        startTime: newEvent.value.startTime,
        endTime: newEvent.value.endTime,
        status: newEvent.value.status,
      },
    })
    refetch()
    // Reset form
    newEvent.value = {
      title: '',
      description: '',
      radius: 500,
      latitude: 63.4305,
      longitude: 10.3951,
      level: EventLevel.GREEN,
      startTime: new Date().toISOString(),
      endTime: undefined,
      status: EventStatus.UPCOMING,
    }
  } catch (error) {
    console.error('Error creating event:', error)
    if (error && typeof error === 'object' && 'response' in error) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const apiError = error as { response: { data: any; status: number } }
      console.error('Server response:', apiError.response.data)
      console.error('Status code:', apiError.response.status)
    }
  }
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
