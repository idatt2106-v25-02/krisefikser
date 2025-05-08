<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import ShelterLayer from '@/components/map/layer/ShelterLayer.vue'
import EventLayer from '@/components/map/layer/EventLayer.vue'
import UserLocationLayer from '@/components/map/location/UserLocationLayer.vue'
import HomeLocationLayer from '@/components/map/location/HomeLocationLayer.vue'
import HouseholdMembersLayer from '@/components/map/location/HouseholdMembersLayer.vue'
import MeetingPointLayer from '@/components/map/meetingPoint/MeetingPointLayer.vue'
import MeetingPointForm from '@/components/map/meetingPoint/MeetingPointForm.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point.ts'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type.ts'
import { useGetAllEvents } from '@/api/generated/event/event.ts'
import { useGetActiveHousehold } from '@/api/generated/household/household.ts'
import type {
  MapPointResponse as MapPoint,
  MapPointTypeResponse as MapPointType,
  EventResponse as Event,
  MeetingPointResponse,
} from '@/api/generated/model'
import L from 'leaflet'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'

interface Shelter {
  id: number
  name: string
  position: [number, number]
  capacity: number
}

// Map and related refs
const mapRef = ref<InstanceType<typeof MapComponent> | null>(null)
const userLocationRef = ref<InstanceType<typeof UserLocationLayer> | null>(null)
const mapInstance = ref<L.Map | null>(null)
const userLocationAvailable = ref(false)
const showUserLocation = ref(true)
const userInCrisisZone = ref(false)

// Meeting point state
const showMeetingPointForm = ref(false)
const selectedMeetingPoint = ref<MeetingPointResponse | undefined>(undefined)
const clickedPosition = ref<{ lat: number; lng: number } | undefined>(undefined)
const isAddingMeetingPoint = ref(false)

// Data state
const isLoading = ref(true)
const shelters = ref<Shelter[]>([])
const events = ref<Event[]>([])

// Fetch map data from API
const { data: mapPointsData, isLoading: isLoadingMapPoints } = useGetAllMapPoints()
const { data: mapPointTypesData, isLoading: isLoadingMapPointTypes } = useGetAllMapPointTypes()
const { data: eventsData, isLoading: isLoadingEvents } = useGetAllEvents()
const { data: activeHousehold, isLoading: isLoadingActiveHousehold } = useGetActiveHousehold()

// Computed properties
const isDataLoading = computed(
  () => isLoadingMapPoints.value || isLoadingMapPointTypes.value || isLoadingEvents.value,
)
const canShowMeetingPointLayer = computed(() => mapInstance.value && activeHousehold.value?.id)
const canShowMeetingPointForm = computed(
  () => showMeetingPointForm.value && activeHousehold.value?.id,
)
const householdId = computed(() => activeHousehold.value?.id ?? '')

// Location error dialog state
const showLocationError = ref(false)
const locationError = ref('')

// Process map data
function processMapData() {
  if (!mapPointsData.value || !mapPointTypesData.value) return

  const mapPoints = Array.isArray(mapPointsData.value) ? mapPointsData.value : [mapPointsData.value]
  const mapPointTypes = Array.isArray(mapPointTypesData.value)
    ? mapPointTypesData.value
    : [mapPointTypesData.value]

  const shelterType = mapPointTypes.find((type: MapPointType) =>
    type.title?.toLowerCase().includes('shelter'),
  )

  shelters.value = mapPoints
    .filter((point: MapPoint) => point.type?.id === shelterType?.id)
    .map((point: MapPoint) => ({
      id: Number(point.id) || 0,
      name: point.type?.title || 'Shelter',
      position: [point.latitude || 0, point.longitude || 0],
      capacity: 300,
    }))

  if (eventsData.value) {
    events.value = Array.isArray(eventsData.value) ? eventsData.value : [eventsData.value]
  }

  isLoading.value = false
}

// Watch for data and map instance changes
watch(
  [isDataLoading, mapInstance],
  ([dataLoaded, map]) => {
    if (!dataLoaded && map) {
      processMapData()
    }
  },
  { immediate: true },
)

// Handle map instance being set
function onMapCreated(map: L.Map) {
  mapInstance.value = map

  map.on('click', (e: L.LeafletMouseEvent) => {
    if (isAddingMeetingPoint.value) {
      clickedPosition.value = { lat: e.latlng.lat, lng: e.latlng.lng }
      selectedMeetingPoint.value = undefined
      showMeetingPointForm.value = true
      isAddingMeetingPoint.value = false
    }
  })
}

// Handle user location toggle
function toggleUserLocation(show: boolean) {
  if (show) {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          showUserLocation.value = true
          if (userLocationRef.value) {
            userLocationRef.value.setInitialPosition(position)
            userLocationRef.value.toggleUserLocation(true)
          }
        },
        () => {
          locationError.value =
            'Kunne ikke få tilgang til posisjonen din. Vennligst sjekk at du har gitt tillatelse til å bruke posisjon.'
          showLocationError.value = true
          showUserLocation.value = false
        },
      )
    } else {
      locationError.value = 'Din nettleser støtter ikke geolokasjon'
      showLocationError.value = true
      showUserLocation.value = false
    }
  } else {
    showUserLocation.value = false
    userLocationRef.value?.toggleUserLocation(false)
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
function handleMeetingPointClick(point: MeetingPointResponse) {
  selectedMeetingPoint.value = point
  clickedPosition.value = undefined
  showMeetingPointForm.value = true
}

// Handle meeting point form close
function handleMeetingPointFormClose() {
  showMeetingPointForm.value = false
  selectedMeetingPoint.value = undefined
  clickedPosition.value = undefined
  isAddingMeetingPoint.value = false
}

// Toggle meeting point creation
function toggleMeetingPointCreation() {
  isAddingMeetingPoint.value = !isAddingMeetingPoint.value
}

// Initialize location on mount
onMounted(() => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        showUserLocation.value = true
        if (userLocationRef.value) {
          userLocationRef.value.setInitialPosition(position)
          userLocationRef.value.toggleUserLocation(true)
        }
      },
      () => {
        locationError.value =
          'Kunne ikke få tilgang til posisjonen din. Vennligst sjekk at du har gitt tillatelse til å bruke posisjon.'
        showLocationError.value = true
        showUserLocation.value = false
      },
    )
  } else {
    locationError.value = 'Din nettleser støtter ikke geolokasjon'
    showLocationError.value = true
    showUserLocation.value = false
  }
})
</script>

<template>
  <div class="relative w-full h-screen overflow-hidden">
    <MapComponent ref="mapRef" @map-created="onMapCreated" />

    <div
      v-if="isLoading || isLoadingActiveHousehold"
      class="absolute top-0 left-0 right-0 bg-slate-700 text-white p-2 text-center"
    >
      Laster kartdata...
    </div>

    <template v-if="!isLoading && !isLoadingActiveHousehold">
      <ShelterLayer v-if="mapInstance" :map="mapInstance as any" :shelters="shelters" />
      <EventLayer v-if="mapInstance" :map="mapInstance as any" :events="events" />
      <UserLocationLayer
        v-if="mapInstance"
        ref="userLocationRef"
        :map="mapInstance as any"
        :events="events"
        @user-in-crisis-zone="handleUserCrisisZoneChange"
        @user-location-available="onUserLocationStatus"
      />
      <HomeLocationLayer
        v-if="
          activeHousehold?.id &&
          mapInstance &&
          activeHousehold.latitude &&
          activeHousehold.longitude
        "
        :map="mapInstance as any"
        :home-location="{
          latitude: activeHousehold.latitude,
          longitude: activeHousehold.longitude,
        }"
      />
      <HouseholdMembersLayer v-if="activeHousehold?.id && mapInstance" :map="mapInstance as any" />
      <MeetingPointLayer
        v-if="canShowMeetingPointLayer && mapInstance"
        :map="mapInstance as any"
        :household-id="householdId"
        @meeting-point-clicked="handleMeetingPointClick"
      />
    </template>

    <MapLegend
      :user-location-available="userLocationAvailable"
      :show-user-location="showUserLocation"
      :user-in-crisis-zone="userInCrisisZone"
      :is-adding-meeting-point="isAddingMeetingPoint"
      :has-active-household="!!activeHousehold?.id"
      @toggle-user-location="toggleUserLocation"
      @toggle-meeting-point-creation="toggleMeetingPointCreation"
    />

    <!-- Location Error Dialog -->
    <Dialog :open="showLocationError" @update:open="(val) => !val && (showLocationError = false)">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Posisjonstilgang</DialogTitle>
          <DialogDescription>
            {{ locationError }}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter class="flex gap-2 mt-4">
          <Button variant="default" @click="showLocationError = false" class="flex-1"> OK </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Meeting Point Form Dialog -->
    <Dialog
      :open="!!canShowMeetingPointForm"
      @update:open="(val) => !val && handleMeetingPointFormClose()"
    >
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{{
            selectedMeetingPoint ? 'Rediger møteplass' : 'Ny møteplass'
          }}</DialogTitle>
        </DialogHeader>
        <MeetingPointForm
          :household-id="householdId"
          :point="selectedMeetingPoint"
          :position="clickedPosition"
          @close="handleMeetingPointFormClose"
          @saved="handleMeetingPointFormClose"
        />
      </DialogContent>
    </Dialog>
  </div>
</template>

<style scoped>
/* Prevent scrolling on the map page */
html,
body {
  height: 100%;
  overflow: hidden;
}
</style>
