<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'

// Fix for the marker icon issue in Leaflet
import { Marker } from 'leaflet'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

// Trondheim center coordinates
const TRONDHEIM_CENTER: [number, number] = [63.4305, 10.3951]

// Map instance ref
const mapInstance = ref<L.Map | null>(null)

// Define props
const props = defineProps<{
  initialZoom?: number
  darkMode?: boolean
}>()

// Define emits
const emit = defineEmits(['map-created'])

// Determine map style based on dark mode
const mapStyle = computed(() => (props.darkMode ? 'mapbox/dark-v11' : 'mapbox/streets-v12'))

// Then separately fix the Leaflet marker icon issue

// eslint-disable-next-line @typescript-eslint/no-explicit-any
;(Marker.prototype as any)._getIconUrl = function () {
  return ''
}

Marker.mergeOptions({
  icon: L.icon({
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon2x,
    shadowUrl: markerShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    tooltipAnchor: [16, -28],
    shadowSize: [41, 41],
  }),
})

// Custom zoom control HTML
const setupCustomControls = (map: L.Map) => {
  // Create custom zoom in control
  const zoomInControl = L.control({ position: 'bottomleft' })
  zoomInControl.onAdd = function () {
    const div = L.DomUtil.create('div', 'custom-map-control zoom-in')
    div.innerHTML = `
      <button aria-label="Zoom inn" class="bg-background text-foreground p-2 rounded-md shadow-md hover:bg-accent transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
      </button>
    `
    L.DomEvent.on(div, 'click', function (e) {
      L.DomEvent.stopPropagation(e)
      map.zoomIn()
    })
    return div
  }
  zoomInControl.addTo(map)

  // Create custom zoom out control
  const zoomOutControl = L.control({ position: 'bottomleft' })
  zoomOutControl.onAdd = function () {
    const div = L.DomUtil.create('div', 'custom-map-control zoom-out')
    div.innerHTML = `
      <button aria-label="Zoom ut" class="bg-background text-foreground p-2 rounded-md shadow-md hover:bg-accent transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
      </button>
    `
    L.DomEvent.on(div, 'click', function (e) {
      L.DomEvent.stopPropagation(e)
      map.zoomOut()
    })
    return div
  }
  zoomOutControl.addTo(map)

  // Create home button to reset view
  const homeControl = L.control({ position: 'bottomleft' })
  homeControl.onAdd = function () {
    const div = L.DomUtil.create('div', 'custom-map-control home')
    div.innerHTML = `
      <button aria-label="Reset visning" class="bg-background text-foreground p-2 rounded-md shadow-md hover:bg-accent transition-colors">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
          <polyline points="9 22 9 12 15 12 15 22"></polyline>
        </svg>
      </button>
    `
    L.DomEvent.on(div, 'click', function (e) {
      L.DomEvent.stopPropagation(e)
      map.setView(TRONDHEIM_CENTER, props.initialZoom || 13)
    })
    return div
  }
  homeControl.addTo(map)
}

// Initialize map
onMounted(() => {
  try {
    // Create map
    const map = L.map('map', {
      zoomControl: false, // We'll use custom controls
      scrollWheelZoom: true,
      maxZoom: 18,
      minZoom: 3,
      attributionControl: false, // We'll add it back in a better position
    }).setView(TRONDHEIM_CENTER, props.initialZoom || 13)
    mapInstance.value = map

    // Add Mapbox tiles with style that matches the app theme
    L.tileLayer(
      'https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}',
      {
        id: mapStyle.value,
        accessToken: import.meta.env.VITE_MAPBOX_ACCESS_TOKEN,
        tileSize: 512,
        zoomOffset: -1,
      } as L.TileLayerOptions & { accessToken: string },
    ).addTo(map)

    // Add custom controls
    setupCustomControls(map)

    // Add attribution control in better position
    L.control
      .attribution({
        position: 'bottomleft',
      })
      .addTo(map)

    // Emit map created event
    emit('map-created', map)
  } catch (error) {
    console.error('Error initializing map', error)
  }
})

// Cleanup
onUnmounted(() => {
  if (mapInstance.value) {
    mapInstance.value.remove()
  }
})

// Define exposed properties
defineExpose({
  mapInstance,
})
</script>

<template>
  <div id="map" class="w-full h-full z-[1] rounded-lg shadow-md overflow-hidden"></div>
</template>

<style scoped>
:deep(.leaflet-control-zoom),
:deep(.custom-map-control) {
  margin-bottom: 8px !important;
}

:deep(.leaflet-control-attribution) {
  background-color: rgba(var(--background), 0.8);
  color: var(--foreground);
  font-size: 11px;
  border-top-right-radius: 4px;
}

:deep(.custom-map-control button) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  margin-bottom: 5px;
  cursor: pointer;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: var(--radius);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

:deep(.leaflet-popup-tip) {
  background: var(--background);
}

:deep(.leaflet-popup-content-wrapper) {
  background: var(--background);
  color: var(--foreground);
}

:deep(.leaflet-container) {
  font-family: inherit;
}
</style>
