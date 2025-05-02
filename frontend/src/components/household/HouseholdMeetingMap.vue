<script setup lang="ts">
import { ref } from 'vue'
import type { PropType } from 'vue' // Use type-only import for PropType
import MapComponent from '@/components/map/MapComponent.vue'
import UserLocationLayer from '@/components/map/UserLocationLayer.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import L from 'leaflet'

// Define interfaces
interface MeetingPlace {
  id: string
  name: string
  address: string
  position: [number, number] // [latitude, longitude]
  description?: string
  type: 'primary' | 'secondary' // Primary or secondary meeting place
}

// Define interface for UserLocationLayer component to help TypeScript understand its structure
interface UserLocationLayerInstance {
  userPosition: [number, number] | null
  toggleUserLocation: (show: boolean) => void
}

// Props definition
const props = defineProps({
  meetingPlaces: {
    type: Array as PropType<MeetingPlace[]>,
    required: true,
  },
  householdPosition: {
    type: Array as unknown as PropType<[number, number]>, // Cast with unknown first
    default: () => null as unknown as [number, number] | null,
  },
})

// Map and related refs
const mapRef = ref<InstanceType<typeof MapComponent> | null>(null)
const userLocationRef = ref<UserLocationLayerInstance | null>(null)
const mapInstance = ref<L.Map | null>(null)
const userLocationAvailable = ref(false)
const showUserLocation = ref(false)
const userInCrisisZone = ref(false)
const selectedMeetingPlace = ref<MeetingPlace | null>(null)
const directionsVisible = ref(false)
const isAddingMeetingPoint = ref(false)
const hasActiveHousehold = ref(true)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const directionsRoute = ref<any[]>([])

// Emits for map interactions
const emit = defineEmits(['meeting-place-selected', 'get-directions'])

// Handle user entering/leaving crisis zone
function handleUserCrisisZoneChange(inZone: boolean) {
  userInCrisisZone.value = inZone
}

// Handle map instance being set
function onMapCreated(map: L.Map) {
  mapInstance.value = map

  // Add meeting places to the map
  if (mapInstance.value && props.meetingPlaces.length > 0) {
    addMeetingPlacesToMap()
  }

  // Center map on household location or first meeting place
  if (props.householdPosition) {
    mapInstance.value.setView(props.householdPosition as L.LatLngExpression, 13)
  } else if (props.meetingPlaces.length > 0) {
    mapInstance.value.setView(props.meetingPlaces[0].position as L.LatLngExpression, 13)
  }
}

// Add meeting places markers to the map
function addMeetingPlacesToMap() {
  if (!mapInstance.value) return

  props.meetingPlaces.forEach((place) => {
    // Create custom icon based on meeting place type
    const icon = L.divIcon({
      html: `<div class="${place.type === 'primary' ? 'bg-red-500' : 'bg-orange-400'} rounded-full p-1 flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="white" class="w-5 h-5">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>`,
      className: 'meeting-place-icon',
      iconSize: [28, 28],
      iconAnchor: [14, 28],
      popupAnchor: [0, -28],
    })

    // Create marker with popup
    const marker = L.marker(place.position as L.LatLngExpression, { icon }).addTo(
      mapInstance.value as unknown as L.Map,
    ).bindPopup(`
        <div class="p-2">
          <h3 class="font-bold">${place.name}</h3>
          <p class="text-sm text-gray-600">${place.address}</p>
          ${place.description ? `<p class="text-sm mt-1">${place.description}</p>` : ''}
          <div class="mt-2">
            <button class="bg-blue-500 hover:bg-blue-600 text-white py-1 px-2 rounded text-xs get-directions">Veibeskrivelse</button>
          </div>
        </div>
      `)

    // Handle popup events
    marker.on('popupopen', () => {
      selectedMeetingPlace.value = place
      emit('meeting-place-selected', place)

      // Add event listener to directions button in popup
      setTimeout(() => {
        const dirButton = document.querySelector('.get-directions')
        if (dirButton) {
          dirButton.addEventListener('click', () => getDirections(place))
        }
      }, 100)
    })
  })
}

// Get directions from user location to meeting place
function getDirections(place: MeetingPlace) {
  if (!userLocationRef.value || !userLocationRef.value.userPosition) {
    alert('Din posisjon er ikke tilgjengelig. Aktiver posisjonsdeling for å få veibeskrivelse.')
    return
  }

  const userPos = userLocationRef.value.userPosition
  directionsVisible.value = true

  // Mock directions (in a real app, you would use a routing API)
  const mockRoute = [
    [userPos[0], userPos[1]],
    [
      userPos[0] + (place.position[0] - userPos[0]) * 0.3,
      userPos[1] + (place.position[1] - userPos[1]) * 0.3,
    ],
    [
      userPos[0] + (place.position[0] - userPos[0]) * 0.7,
      userPos[1] + (place.position[1] - userPos[1]) * 0.7,
    ],
    [place.position[0], place.position[1]],
  ]

  directionsRoute.value = mockRoute

  // Draw route line on map
  if (mapInstance.value) {
    L.polyline(mockRoute as L.LatLngExpression[], {
      color: 'blue',
      weight: 4,
      opacity: 0.7,
      dashArray: '10, 10',
    }).addTo(mapInstance.value as unknown as L.Map)
  }

  emit('get-directions', {
    from: userPos,
    to: place.position,
    place: place,
  })
}

// Handle user location toggle
function toggleUserLocation(show: boolean) {
  showUserLocation.value = show
  if (userLocationRef.value) {
    userLocationRef.value.toggleUserLocation(show)
  }
}

// Get user location availability status
function onUserLocationStatus(available: boolean) {
  userLocationAvailable.value = available
}

// Method to center map on a specific meeting place
function centerOnMeetingPlace(placeId: string) {
  const place = props.meetingPlaces.find((p) => p.id === placeId)
  if (place && mapInstance.value) {
    mapInstance.value.setView(place.position as L.LatLngExpression, 15)

    // Find and open the marker popup
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    mapInstance.value.eachLayer((layer: any) => {
      if (layer instanceof L.Marker) {
        const pos = layer.getLatLng()
        if (pos.lat === place.position[0] && pos.lng === place.position[1]) {
          layer.openPopup()
        }
      }
    })
  }
}

// Expose methods to parent component
defineExpose({
  centerOnMeetingPlace,
})
</script>

<template>
  <div class="relative w-full h-[500px] border border-gray-200 rounded-lg overflow-hidden">
    <MapComponent ref="mapRef" @map-created="onMapCreated" />

    <UserLocationLayer
      ref="userLocationRef"
      :map="mapInstance as any"
      :events="[]"
      @user-in-crisis-zone="handleUserCrisisZoneChange"
      @user-location-available="onUserLocationStatus"
    />

    <MapLegend
      :user-location-available="userLocationAvailable"
      :show-user-location="showUserLocation"
      :user-in-crisis-zone="userInCrisisZone"
      :is-adding-meeting-point="isAddingMeetingPoint"
      :has-active-household="hasActiveHousehold"
      @toggle-user-location="toggleUserLocation"
    />

    <!-- Meeting place info overlay - shown when a place is selected -->
    <div
      v-if="selectedMeetingPlace"
      class="absolute bottom-16 left-4 right-4 bg-white p-3 rounded-lg shadow-md max-w-sm"
    >
      <div class="flex justify-between items-start">
        <div>
          <h3 class="font-bold text-gray-800">{{ selectedMeetingPlace.name }}</h3>
          <p class="text-sm text-gray-600">{{ selectedMeetingPlace.address }}</p>
        </div>
        <button class="text-gray-500 hover:text-gray-700" @click="selectedMeetingPlace = null">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clip-rule="evenodd"
            />
          </svg>
        </button>
      </div>

      <div v-if="selectedMeetingPlace.description" class="mt-2 text-sm">
        {{ selectedMeetingPlace.description }}
      </div>

      <div class="mt-3">
        <button
          class="bg-blue-600 hover:bg-blue-700 text-white py-1.5 px-3 rounded-md text-sm w-full"
          @click="getDirections(selectedMeetingPlace)"
          :disabled="!userLocationAvailable"
        >
          Få veibeskrivelse
        </button>
      </div>
    </div>

    <!-- Directions overlay - shown when directions are requested -->
    <div
      v-if="directionsVisible && selectedMeetingPlace"
      class="absolute top-4 right-4 bg-white p-3 rounded-lg shadow-md max-w-sm"
    >
      <div class="flex justify-between items-center mb-2">
        <h3 class="font-bold text-gray-800 text-sm">
          Veibeskrivelse til {{ selectedMeetingPlace.name }}
        </h3>
        <button class="text-gray-500 hover:text-gray-700" @click="directionsVisible = false">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clip-rule="evenodd"
            />
          </svg>
        </button>
      </div>

      <div class="text-xs text-gray-600">
        <p>Følg den blå markerte ruten på kartet.</p>
        <p class="mt-1">Estimert avstand: 2.3 km</p>
        <p>Estimert tid: 28 min (til fots)</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.meeting-place-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
