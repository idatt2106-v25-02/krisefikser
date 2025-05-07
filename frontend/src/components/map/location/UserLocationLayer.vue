<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import L from 'leaflet'
import type { EventResponse as Event } from '@/api/generated/model'
import {
  EventResponseLevel as EventLevel,
  EventResponseStatus as EventStatus,
} from '@/api/generated/model'

// Define props
const props = defineProps<{
  map: L.Map | null
  events?: Event[]
}>()

// Define emits
const emit = defineEmits(['userInCrisisZone', 'user-location-available'])

// Component state
const userMarker = ref<L.Marker | null>(null)
const userLocationAvailable = ref(false)
const watchId = ref<number | null>(null)
const userInCrisisZone = ref(false)
const currentPosition = ref<GeolocationPosition | null>(null)
const isInitialized = ref(false)

// Check if user is in event crisis zone
function checkUserInEventZone(position: GeolocationPosition) {
  if (!props.map || !props.events || props.events.length === 0) return

  const { latitude, longitude } = position.coords
  const wasInCrisisZone = userInCrisisZone.value
  userInCrisisZone.value = false

  // Only consider active events (ONGOING or UPCOMING) with YELLOW or RED level
  const relevantEvents = props.events.filter(
    (event) =>
      (event.status === EventStatus.ONGOING || event.status === EventStatus.UPCOMING) &&
      (event.level === EventLevel.YELLOW || event.level === EventLevel.RED),
  )

  for (const event of relevantEvents) {
    if (props.map && event.latitude && event.longitude && event.radius) {
      const distance = props.map.distance([latitude, longitude], [event.latitude, event.longitude])

      if (distance <= event.radius) {
        userInCrisisZone.value = true
        emit('userInCrisisZone', true)

        if (!wasInCrisisZone) {
          const levelText = event.level === EventLevel.RED ? 'HØYT VARSEL' : 'ADVARSEL'
          alert(`${levelText}: Du er i "${event.title}"-området.\n${event.description}`)
        }
        return
      }
    }
  }

  if (wasInCrisisZone) {
    emit('userInCrisisZone', false)
  }
}

// Create user marker
function createUserMarker(position: GeolocationPosition) {
  if (!props.map) return null

  const { latitude, longitude } = position.coords

  return L.marker([latitude, longitude], {
    icon: L.divIcon({
      className: 'user-location-marker',
      html: `
        <div class="">
          <div class="pulse bg-blue-500 z-20 rounded-full w-8 h-8 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
        </div>
      `,
      iconSize: [32, 32],
      iconAnchor: [16, 16],
      popupAnchor: [0, -16]
    }),
    zIndexOffset: 1000
  }).addTo(props.map)
}

// Set initial position
function setInitialPosition(position: GeolocationPosition) {
  if (!props.map) return

  currentPosition.value = position
  if (userMarker.value) {
    userMarker.value.remove()
  }
  userMarker.value = createUserMarker(position)
  checkUserInEventZone(position)
  isInitialized.value = true
}

// Watch user position
function startWatchingPosition() {
  if (!navigator.geolocation) {
    userLocationAvailable.value = false
    emit('user-location-available', false)
    return
  }

  userLocationAvailable.value = true
  emit('user-location-available', true)

  // Get initial position first
  navigator.geolocation.getCurrentPosition(
    (position) => {
      setInitialPosition(position)
      // Then start watching
      watchId.value = navigator.geolocation.watchPosition(
        (position) => {
          currentPosition.value = position

          if (!props.map) return

          if (userMarker.value) {
            userMarker.value.setLatLng([position.coords.latitude, position.coords.longitude])
          } else {
            userMarker.value = createUserMarker(position)
          }

          checkUserInEventZone(position)
        },
        () => {
          userLocationAvailable.value = false
          emit('user-location-available', false)
        },
        { enableHighAccuracy: true },
      )
    },
    () => {
      userLocationAvailable.value = false
      emit('user-location-available', false)
    },
    { enableHighAccuracy: true }
  )
}

// Stop watching position
function stopWatchingPosition() {
  if (watchId.value !== null) {
    navigator.geolocation.clearWatch(watchId.value)
    watchId.value = null
  }

  if (userMarker.value && props.map) {
    userMarker.value.remove()
    userMarker.value = null
  }
  isInitialized.value = false
}

// Toggle user location display
function toggleUserLocation(show: boolean) {
  if (show) {
    startWatchingPosition()
  } else {
    stopWatchingPosition()
  }
}

// Watch for map changes
watch(
  () => props.map,
  (newMap) => {
    if (newMap && currentPosition.value && !isInitialized.value) {
      userMarker.value = createUserMarker(currentPosition.value)
      isInitialized.value = true
    }
  },
  { immediate: true }
)

// Watch for events changes
watch(
  () => props.events,
  () => {
    if (currentPosition.value) {
      checkUserInEventZone(currentPosition.value)
    }
  },
  { deep: true },
)

// Initialize on mount
onMounted(() => {
  if (navigator.geolocation) {
    userLocationAvailable.value = true
    emit('user-location-available', true)
    startWatchingPosition()
  } else {
    userLocationAvailable.value = false
    emit('user-location-available', false)
  }
})

// Clean up on unmount
onUnmounted(() => {
  stopWatchingPosition()
})

// Define exposed methods
defineExpose({
  toggleUserLocation,
  setInitialPosition,
})
</script>
