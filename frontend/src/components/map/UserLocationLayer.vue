<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import L from 'leaflet'
import type { Event } from '@/api/generated/model'
import { EventLevel, EventStatus } from '@/api/generated/model'

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

        // Notify parent component
        emit('userInCrisisZone', true)

        // Show alert if newly entered a crisis zone
        if (!wasInCrisisZone) {
          const levelText = event.level === EventLevel.RED ? 'HØYT VARSEL' : 'ADVARSEL'
          alert(`${levelText}: Du er i "${event.title}"-området.\n${event.description}`)
        }
        return
      }
    }
  }

  // If we get here, user is not in any crisis zone
  if (wasInCrisisZone) {
    emit('userInCrisisZone', false)
  }
}

// Set initial position
function setInitialPosition(position: GeolocationPosition) {
  if (!props.map) return

  const { latitude, longitude } = position.coords

  // Create initial marker
  userMarker.value = L.marker([latitude, longitude], {
    icon: L.divIcon({
      className: 'user-location-marker',
      html: '<div class="pulse bg-red-500 rounded-full w-10 h-10"></div>',
      iconSize: [20, 20],
      iconAnchor: [10, 10],
    }),
  }).addTo(props.map)

  // Check if user is in an event crisis zone
  checkUserInEventZone(position)
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

  // Watch position
  watchId.value = navigator.geolocation.watchPosition(
    (position) => {
      console.log('Position updated', position)

      const { latitude, longitude } = position.coords

      if (!props.map) return

      // Update user marker position
      if (userMarker.value) {
        console.log('Updating marker position')
        userMarker.value.setLatLng([latitude, longitude])
      } else {
        // Create marker if it doesn't exist
        console.log('Creating marker')
        userMarker.value = L.marker([latitude, longitude], {
          icon: L.divIcon({
            className: 'user-location-marker',
            html: '<div class="pulse"></div>',
            iconSize: [20, 20],
            iconAnchor: [10, 10],
          }),
        }).addTo(props.map)
      }

      // Check if user is in an event crisis zone
      checkUserInEventZone(position)
    },
    (error) => {
      console.error('Error getting user location', error)
      userLocationAvailable.value = false
      emit('user-location-available', false)
    },
    { enableHighAccuracy: true },
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
}

// Toggle user location display
function toggleUserLocation(show: boolean) {
  if (show) {
    startWatchingPosition()
  } else {
    stopWatchingPosition()
  }
}

// Check location capability on mount
onMounted(() => {
  if (navigator.geolocation) {
    userLocationAvailable.value = true
    emit('user-location-available', true)
  } else {
    userLocationAvailable.value = false
    emit('user-location-available', false)
  }
})

// Watch for map changes
watch(
  () => props.map,
  (newMap) => {
    if (newMap && userMarker.value) {
      userMarker.value.addTo(newMap)
    }
  },
)

// Watch for events changes
watch(
  () => props.events,
  () => {
    if (userMarker.value && props.map) {
      const position = userMarker.value.getLatLng()
      const geolocationPosition = {
        coords: {
          latitude: position.lat,
          longitude: position.lng,
          accuracy: 0,
          altitude: null,
          altitudeAccuracy: null,
          heading: null,
          speed: null,
        },
        timestamp: Date.now(),
      } as GeolocationPosition

      checkUserInEventZone(geolocationPosition)
    }
  },
  { deep: true },
)

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
