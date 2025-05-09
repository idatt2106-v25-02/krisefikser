<script lang="ts" setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
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

const userLocationIcon = L.icon({
  iconUrl: '/icons/map/user.svg',
  iconSize: [32, 32],
  iconAnchor: [16, 16],
  popupAnchor: [0, -16],
})

// Define emits
const emit = defineEmits(['userInCrisisZone', 'user-location-available'])

// Component state
const userMarker = ref<L.Marker | null>(null)
const userLocationAvailable = ref(false)
const watchId = ref<number | null>(null)
const userInCrisisZone = ref(false)
const currentPosition = ref<GeolocationPosition | null>(null)
const isInitialized = ref(false)

// Alert state
const showAlert = ref(false)
const alertTitle = ref('')
const alertMessage = ref('')
const alertType = ref<'warning' | 'danger'>('warning')

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
          const isRed = event.level === EventLevel.RED
          alertType.value = isRed ? 'danger' : 'warning'
          alertTitle.value = isRed ? 'HØYT VARSEL' : 'ADVARSEL'
          alertMessage.value = `Du er i "${event.title}"-området.\n${event.description}`
          showAlert.value = true
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
    icon: userLocationIcon,
    zIndexOffset: 1000,
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
  { immediate: true },
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
<template>
  <!-- Alert Component -->
  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="transform -translate-y-4 opacity-0"
    enter-to-class="transform translate-y-0 opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="transform translate-y-0 opacity-100"
    leave-to-class="transform -translate-y-4 opacity-0"
  >
    <div
      v-if="showAlert"
      class="fixed top-4 left-1/2 transform -translate-x-1/2 z-50 w-full max-w-md mx-auto"
    >
      <div
        :class="[
          'rounded-lg shadow-lg p-4 flex items-start space-x-4',
          alertType === 'danger'
            ? 'bg-red-50 border-2 border-red-500'
            : 'bg-yellow-50 border-2 border-yellow-500',
        ]"
      >
        <div class="flex-shrink-0">
          <svg
            v-if="alertType === 'danger'"
            class="h-6 w-6 text-red-600"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
          <svg
            v-else
            class="h-6 w-6 text-yellow-600"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
        <div class="flex-1">
          <h3
            :class="[
              'text-lg font-bold',
              alertType === 'danger' ? 'text-red-800' : 'text-yellow-800',
            ]"
          >
            {{ alertTitle }}
          </h3>
          <p
            :class="[
              'mt-1 text-sm whitespace-pre-line',
              alertType === 'danger' ? 'text-red-700' : 'text-yellow-700',
            ]"
          >
            {{ alertMessage }}
          </p>
        </div>
        <button
          @click="showAlert = false"
          class="flex-shrink-0 ml-4"
          :class="[
            'rounded-md inline-flex p-1.5 focus:outline-none focus:ring-2 focus:ring-offset-2',
            alertType === 'danger'
              ? 'text-red-500 hover:bg-red-100 focus:ring-red-500'
              : 'text-yellow-500 hover:bg-yellow-100 focus:ring-yellow-500',
          ]"
        >
          <span class="sr-only">Lukk</span>
          <svg
            class="h-5 w-5"
            xmlns="http://www.w3.org/2000/svg"
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
    </div>
  </Transition>
</template>

<style scoped>
.user-location-marker {
  position: relative;
}

.pulse {
  position: relative;
}

.pulse::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background-color: rgb(59 130 246 / 0.5);
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.5);
  }
}
</style>
