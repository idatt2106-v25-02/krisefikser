<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import L from 'leaflet'

// Define props
const props = defineProps<{
  map: L.Map | null
  homeLocation?: { latitude: number; longitude: number } | null
}>()

// Component state
const homeMarker = ref<L.Marker | null>(null)

const homeDivIcon = L.divIcon({
  className: 'home-location-marker',
  html: `
    <div class="relative">
      <div class="w-10 h-10 flex items-center justify-center">
        <svg width="52" height="52" viewBox="0 0 52 52" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="2" y="2" width="48" height="48" rx="16" fill="#DCE5F9"/>
          <rect x="2" y="2" width="48" height="48" rx="16" fill="#DCE5F9"/>
          <rect x="2" y="2" width="48" height="48" rx="16" stroke="#364153" stroke-width="3"/>
          <rect x="2" y="2" width="48" height="48" rx="16" stroke="#364153" stroke-width="3"/>
          <path d="M37.7675 23.2312L27.7675 13.2312C27.2987 12.7626 26.6629 12.4993 26 12.4993C25.3371 12.4993 24.7013 12.7626 24.2325 13.2312L14.2325 23.2312C13.9994 23.4629 13.8146 23.7385 13.6889 24.0422C13.5632 24.3458 13.499 24.6714 13.5 25V37C13.5 37.3978 13.658 37.7793 13.9394 38.0606C14.2207 38.3419 14.6022 38.5 15 38.5H23C23.3978 38.5 23.7794 38.3419 24.0607 38.0606C24.342 37.7793 24.5 37.3978 24.5 37V30.5H27.5V37C27.5 37.3978 27.658 37.7793 27.9394 38.0606C28.2207 38.3419 28.6022 38.5 29 38.5H37C37.3978 38.5 37.7794 38.3419 38.0607 38.0606C38.342 37.7793 38.5 37.3978 38.5 37V25C38.5011 24.6714 38.4369 24.3458 38.3111 24.0422C38.1854 23.7385 38.0006 23.4629 37.7675 23.2312ZM35.5 35.5H30.5V29C30.5 28.6022 30.342 28.2206 30.0607 27.9393C29.7794 27.658 29.3978 27.5 29 27.5H23C22.6022 27.5 22.2207 27.658 21.9394 27.9393C21.658 28.2206 21.5 28.6022 21.5 29V35.5H16.5V25.2062L26 15.7062L35.5 25.2062V35.5Z" fill="#364153"/>
        </svg>
      </div>
    </div>
    `,
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
})

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
    icon: homeDivIcon,
    zIndexOffset: 900,
  }).addTo(props.map).bindPopup(`
      <div style="min-width: 300px; font-family: system-ui, -apple-system, sans-serif; border-radius: 8px; overflow: hidden;">
        <div style="padding: 16px;">
          <h3 style="margin: 0 0 12px; color: #1f2937; font-size: 18px; font-weight: 700;">Aktiv husstand</h3>

          <!-- Metadata badge -->
          <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px;">
            <span style="display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 500; background-color: #36415320; color: #364153;">
              <svg xmlns="http://www.w3.org/2000/svg" style="height: 12px; width: 12px; margin-right: 4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
              </svg>
              Hjemmeadresse
            </span>
          </div>

          <!-- View button -->
          <a href="/husstand" style="display: inline-flex; align-items: center; justify-content: center; background-color: #10B981; color: white; font-weight: 500; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 14px; transition: background-color 0.2s;">
            <svg xmlns="http://www.w3.org/2000/svg" style="width: 16px; height: 16px; margin-right: 6px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14"></path>
              <path d="m12 5 7 7-7 7"></path>
            </svg>
            Gå til husstand
          </a>
        </div>
      </div>
    `)
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
  <template></template>
</template>
