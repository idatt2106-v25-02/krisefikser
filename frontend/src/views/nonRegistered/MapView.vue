<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import ShelterLayer from '@/components/map/ShelterLayer.vue'
import EventLayer from '@/components/map/EventLayer.vue'
import UserLocationLayer from '@/components/map/UserLocationLayer.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllEvents } from '@/api/generated/event/event'
import type { MapPoint, MapPointType } from '@/api/generated/model'
import L from 'leaflet'
import type { Event } from '@/api/generated/model'
import { webSocket } from '@/main.ts'

// Map and related refs
const mapRef = ref<InstanceType<typeof MapComponent> | null>(null)
const userLocationRef = ref<InstanceType<typeof UserLocationLayer> | null>(null)
const mapInstance = ref<L.Map | null>(null)
const userLocationAvailable = ref(false)
const showUserLocation = ref(false)
const userInCrisisZone = ref(false)

// Data fetching and state
const isLoading = ref(true)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const shelters = ref<any[]>([])
const events = ref<Event[]>([])
const topics = ['/topic/events', '/topic/events/new', '/topic/events/delete']

// Fetch map data from API
const { data: mapPointsData, isLoading: isLoadingMapPoints } = useGetAllMapPoints()
const { data: mapPointTypesData, isLoading: isLoadingMapPointTypes } = useGetAllMapPointTypes()
const { data: eventsData, isLoading: isLoadingEvents } = useGetAllEvents()

webSocket.subscribe<Event>('/topic/events/new', (message: Event) => {
  events.value.push(message)
})
webSocket.subscribe<number>('/topic/events/delete', (message: number) => {
  events.value = events.value.filter((event: Event) => event.id !== message)
})
webSocket.subscribe<Event>('/topic/events', (message: Event) => {
  events.value = events.value.map((event: Event) => {
    if (event.id === message.id) {
      return message
    }
    return event
  })
})

function disconnectWebSocket() {
  topics.forEach((topic) => {
    webSocket.unsubscribe(topic)
  })
}

// Process map data
function processMapData() {
  if (!mapPointsData.value || !mapPointTypesData.value) return

  // Get data arrays from the API response
  const mapPoints = Array.isArray(mapPointsData.value) ? mapPointsData.value : [mapPointsData.value]

  const mapPointTypes = Array.isArray(mapPointTypesData.value)
    ? mapPointTypesData.value
    : [mapPointTypesData.value]

  // Find shelter type
  const shelterType = mapPointTypes.find((type: MapPointType) =>
    type.title?.toLowerCase().includes('shelter'),
  )

  // Process shelter points
  shelters.value = mapPoints
    .filter((point: MapPoint) => point.type?.id === shelterType?.id)
    .map((point: MapPoint) => ({
      id: point.id,
      name: point.type?.title || 'Shelter',
      position: [point.latitude || 0, point.longitude || 0],
      capacity: 300, // Default capacity value
    }))

  // Process events if available
  if (eventsData.value) {
    events.value = Array.isArray(eventsData.value) ? eventsData.value : [eventsData.value]
  }

  isLoading.value = false
}

// Watch for data load completion
watch(
  [isLoadingMapPoints, isLoadingMapPointTypes, isLoadingEvents],
  ([mapPointsLoading, mapPointTypesLoading, eventsLoading]) => {
    if (!mapPointsLoading && !mapPointTypesLoading && !eventsLoading) {
      processMapData()
    }
  },
)

// Handle map instance being set
function onMapCreated(map: L.Map) {
  mapInstance.value = map
}

// Handle user location toggle
function toggleUserLocation(show: boolean) {
  showUserLocation.value = show
  if (userLocationRef.value) {
    userLocationRef.value.toggleUserLocation(show)
  }
}

// Handle user entering/leaving crisis zone
function handleUserCrisisZoneChange(inZone: boolean) {
  userInCrisisZone.value = inZone
}

// Get user location availability status
function onUserLocationStatus(available: boolean) {
  userLocationAvailable.value = available
}

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<template>
  <div class="relative w-full h-screen">
    <MapComponent ref="mapRef" @map-created="onMapCreated" />

    <div
      v-if="isLoading"
      class="absolute top-0 left-0 right-0 bg-slate-700 text-white p-2 text-center"
    >
      Laster kartdata...
    </div>

    <template v-if="!isLoading">
      <ShelterLayer :map="mapInstance" :shelters="shelters" />

      <EventLayer :map="mapInstance" :events="events" />

      <UserLocationLayer
        ref="userLocationRef"
        :map="mapInstance"
        :events="events"
        @user-in-crisis-zone="handleUserCrisisZoneChange"
        @user-location-available="onUserLocationStatus"
      />
    </template>

    <MapLegend
      :user-location-available="userLocationAvailable"
      :show-user-location="showUserLocation"
      :user-in-crisis-zone="userInCrisisZone"
      @toggle-user-location="toggleUserLocation"
    />
  </div>
</template>
