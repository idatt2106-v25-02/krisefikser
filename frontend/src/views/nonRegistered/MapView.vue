<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import ShelterLayer from '@/components/map/ShelterLayer.vue'
import EventLayer from '@/components/map/EventLayer.vue'
import UserLocationLayer from '@/components/map/UserLocationLayer.vue'
import MeetingPointLayer from '@/components/map/MeetingPointLayer.vue'
import MeetingPointForm from '@/components/map/MeetingPointForm.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllEvents } from '@/api/generated/event/event'
import { useGetActiveHousehold } from '@/api/generated/household/household'
import type { MapPoint, MapPointType } from '@/api/generated/model'
import type { Event } from '@/api/generated/model'
import type { MeetingPoint } from '@/api/generated/model'
import L from 'leaflet'

// Map and related refs
const mapRef = ref<InstanceType<typeof MapComponent> | null>(null)
const userLocationRef = ref<InstanceType<typeof UserLocationLayer> | null>(null)
const mapInstance = ref<L.Map | null>(null)
const userLocationAvailable = ref(false)
const showUserLocation = ref(false)
const userInCrisisZone = ref(false)

// Meeting point state
const showMeetingPointForm = ref(false)
const selectedMeetingPoint = ref<MeetingPoint | null>(null)
const clickedPosition = ref<{ lat: number; lng: number } | null>(null)

// Get active household
const { data: activeHousehold, isLoading: isLoadingActiveHousehold } = useGetActiveHousehold()

// Data fetching and state
const isLoading = ref(true)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const shelters = ref<any[]>([])
const events = ref<Event[]>([])

// Fetch map data from API
const { data: mapPointsData, isLoading: isLoadingMapPoints } = useGetAllMapPoints()
const { data: mapPointTypesData, isLoading: isLoadingMapPointTypes } = useGetAllMapPointTypes()
const { data: eventsData, isLoading: isLoadingEvents } = useGetAllEvents()

// Process map data
function processMapData() {
  if (!mapPointsData.value || !mapPointTypesData.value) return

  // Get data arrays from the API response
  const mapPoints = Array.isArray(mapPointsData.value)
    ? mapPointsData.value
    : [mapPointsData.value]

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
onMounted(() => {
  const checkDataLoaded = () => {
    if (!isLoadingMapPoints.value && !isLoadingMapPointTypes.value && !isLoadingEvents.value) {
      processMapData()
    } else {
      setTimeout(checkDataLoaded, 100)
    }
  }

  checkDataLoaded()
})

// Handle map instance being set
function onMapCreated(map: L.Map) {
  mapInstance.value = map

  // Add click handler for creating new meeting points
  map.on('click', (e: L.LeafletMouseEvent) => {
    clickedPosition.value = { lat: e.latlng.lat, lng: e.latlng.lng }
    selectedMeetingPoint.value = null
    showMeetingPointForm.value = true
  })
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

// Handle meeting point click
function handleMeetingPointClick(point: MeetingPoint) {
  selectedMeetingPoint.value = point
  clickedPosition.value = null
  showMeetingPointForm.value = true
}

// Handle meeting point form close
function handleMeetingPointFormClose() {
  showMeetingPointForm.value = false
  selectedMeetingPoint.value = null
  clickedPosition.value = null
}
</script>

<template>
  <div class="relative w-full h-screen">
    <MapComponent ref="mapRef" @map-created="onMapCreated" />

    <div
      v-if="isLoading || isLoadingActiveHousehold"
      class="absolute top-0 left-0 right-0 bg-slate-700 text-white p-2 text-center"
    >
      Laster kartdata...
    </div>

    <template v-if="!isLoading && !isLoadingActiveHousehold">
      <ShelterLayer :map="mapInstance" :shelters="shelters" />

      <EventLayer :map="mapInstance" :events="events" />

      <UserLocationLayer
        ref="userLocationRef"
        :map="mapInstance"
        :events="events"
        @user-in-crisis-zone="handleUserCrisisZoneChange"
        @user-location-available="onUserLocationStatus"
      />

      <MeetingPointLayer
        v-if="mapInstance && activeHousehold?.id"
        :map="mapInstance"
        :household-id="activeHousehold.id"
        @meeting-point-clicked="handleMeetingPointClick"
      />
    </template>

    <MeetingPointForm
      v-if="showMeetingPointForm && activeHousehold?.id"
      :household-id="activeHousehold.id"
      :point="selectedMeetingPoint"
      :position="clickedPosition"
      @close="handleMeetingPointFormClose"
      @saved="handleMeetingPointFormClose"
    />

    <MapLegend
      :user-location-available="userLocationAvailable"
      :show-user-location="showUserLocation"
      :user-in-crisis-zone="userInCrisisZone"
      @toggle-user-location="toggleUserLocation"
    />
  </div>
</template>

<style scoped>
/* No styles needed as we're using Tailwind classes */
</style>
