<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import L from 'leaflet'

// Define props
const props = defineProps<{
  map: L.Map | null
  homeLocation?: { latitude: number; longitude: number } | null
}>()

// Component state
const homeMarker = ref<L.Marker | null>(null)

// Create or update home marker
function updateHomeMarker() {
  if (!props.map || !props.homeLocation) return

  const { latitude, longitude } = props.homeLocation

  // Remove existing marker if it exists
  if (homeMarker.value) {
    homeMarker.value.remove()
  }

  // Create new marker
  homeMarker.value = L.marker([latitude, longitude], {
    icon: L.divIcon({
      className: 'home-location-marker',
      html: `
        <div class="relative">
          <div class="bg-red-500 rounded-full w-10 h-10 flex items-center justify-center border-2 border-white shadow-lg">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
            </svg>
          </div>
          <div class="absolute -bottom-6 left-1/2 transform -translate-x-1/2 bg-red-500 text-white px-2 py-0.5 rounded whitespace-nowrap shadow-md text-xs font-bold">
            HJEM
          </div>
        </div>
      `,
      iconSize: [40, 40],
      iconAnchor: [20, 20],
      popupAnchor: [0, -20],
    }),
    zIndexOffset: 900,
  }).addTo(props.map)
}

// Watch for changes in the home location
watch(
  () => props.homeLocation,
  () => {
    updateHomeMarker()
  },
  { immediate: true, deep: true },
)

// Watch for map changes
watch(
  () => props.map,
  (newMap) => {
    if (newMap && props.homeLocation) {
      updateHomeMarker()
    }
  },
  { immediate: true },
)

// Clean up on unmount
onMounted(() => {
  updateHomeMarker()
})
</script>
<template>
  <div></div>
</template>
