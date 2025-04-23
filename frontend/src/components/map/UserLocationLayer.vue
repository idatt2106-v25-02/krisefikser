<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import L from 'leaflet'

// Define props
const props = defineProps<{
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  map: any
  crisisAreas?: Array<{
    id: number
    center: [number, number]
    radius: number
    name: string
    severity: number
  }>
}>()

// Define emits
const emit = defineEmits(['userInCrisisZone', 'user-location-available'])

// Component state
const userMarker = ref<L.Marker | null>(null)
const userLocationAvailable = ref(false)
const watchId = ref<number | null>(null)
const userInCrisisZone = ref(false)

// Check if user is in crisis zone
function checkUserInCrisisZone(position: GeolocationPosition) {
  if (!props.map || !props.crisisAreas) return

  const { latitude, longitude } = position.coords
  const wasInCrisisZone = userInCrisisZone.value
  userInCrisisZone.value = false

  for (const area of props.crisisAreas) {
    if (props.map) {
      const distance = props.map.distance([latitude, longitude], area.center)

      if (distance <= area.radius) {
        userInCrisisZone.value = true

        // Notify parent component
        emit('userInCrisisZone', true, area)

        // Show alert if newly entered a crisis zone
        if (!wasInCrisisZone) {
          alert(`ALERT: You are in the ${area.name} area. Severity level: ${area.severity}`)
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
      const { latitude, longitude } = position.coords

      if (!props.map) return

      // Add or update user marker
      if (userMarker.value) {
        userMarker.value.setLatLng([latitude, longitude])
      } else {
        userMarker.value = L.marker([latitude, longitude], {
          icon: L.divIcon({
            className: 'user-location-marker',
            html: '<div class="pulse"></div>',
            iconSize: [20, 20],
            iconAnchor: [10, 10],
          }),
        }).addTo(props.map)
      }

      // Check if user is in crisis zone
      if (props.crisisAreas) {
        checkUserInCrisisZone(position)
      }
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

// Clean up on unmount
onUnmounted(() => {
  stopWatchingPosition()
})

// Define exposed methods
defineExpose({
  toggleUserLocation,
})
</script>

<template>
  <!-- This component doesn't have a visual template as it's just adding a layer to the map -->
  <template></template>
</template>

<style scoped>
/* We'll keep some custom CSS for the animation */
.user-location-marker {
  @apply relative;
}

</style> 
