<script lang="ts" setup>
import { onMounted, watch } from 'vue'
import L from 'leaflet'

// Define props
const props = defineProps<{
  map: L.Map | null
  shelters: Array<{
    id: number
    name: string
    position: [number, number]
    capacity: number
  }>
}>()

// Icon definition
const shelterIcon = L.icon({
  iconUrl:
    'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgiIGhlaWdodD0iNDgiIHZpZXdCb3g9IjAgMCA0OCA0OCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjQ4IiBoZWlnaHQ9IjQ4IiByeD0iMTYiIGZpbGw9IiMzNjQxNTMiLz4KPHBhdGggZD0iTTI4Ljc1IDI0SDE5LjI1VjMzLjVIMjguNzVWMjRaIiBzdHJva2U9IndoaXRlIiBzdHJva2Utd2lkdGg9IjMiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIvPgo8cGF0aCBkPSJNOS43NSAzMy41SDM4LjI1TTM2LjY2NjcgMzMuNUgxMS4zMzMzVjI1LjM1NzFDMTEuMzMzMyAyMi40Nzc2IDEyLjY2NzkgMTkuNzE2MSAxNS4wNDMzIDE3LjY4QzE3LjQxODggMTUuNjQzOSAyMC42NDA2IDE0LjUgMjQgMTQuNUMyNy4zNTk0IDE0LjUgMzAuNTgxMiAxNS42NDM5IDMyLjk1NjcgMTcuNjhDMzUuMzMyMSAxOS43MTYxIDM2LjY2NjcgMjIuNDc3NiAzNi42NjY3IDI1LjM1NzFWMzMuNVoiIHN0cm9rZT0id2hpdGUiIHN0cm9rZS13aWR0aD0iMyIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIi8+Cjwvc3ZnPgo=',
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
})

const markers: L.Marker[] = []

function getGoogleMapsUrl(lat: number, lng: number) {
  return lat && lng ? `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}` : null
}

// Add shelters to map
function addShelters() {
  if (!props.map) return

  // Clear previous markers
  markers.forEach((marker) => marker.remove())
  markers.length = 0

  // Add shelters to map
  for (const shelter of props.shelters) {
    const marker = L.marker(shelter.position, { icon: shelterIcon }).addTo(props.map).bindPopup(`
        <div style="min-width: 300px; font-family: system-ui, -apple-system, sans-serif; border-radius: 8px; overflow: hidden;">
          <div style="padding: 16px;">
            <h3 style="margin: 0 0 12px; color: #1f2937; font-size: 18px; font-weight: 700;">${shelter.name}</h3>

            <!-- Metadata badge -->
            <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px;">
              <span style="display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 500; background-color: #36415320; color: #364153;">
                <svg xmlns="http://www.w3.org/2000/svg" style="height: 12px; width: 12px; margin-right: 4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 2 2 7l10 5 10-5-10-5z"></path>
                  <path d="M2 17l10 5 10-5"></path>
                  <path d="M2 12l10 5 10-5"></path>
                </svg>
                Tilfluktsrom
              </span>
            </div>

            <!-- Capacity information -->
            <div style="display: flex; flex-direction: column; gap: 4px; background: #f9fafb; padding: 10px; border-radius: 6px; margin-bottom: 16px;">
              <div style="display: flex; align-items: center;">
                <svg xmlns="http://www.w3.org/2000/svg" style="height: 14px; width: 14px; margin-right: 6px; color: #6b7280;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                <span style="font-weight: 500; width: 80px; color: #6b7280; font-size: 14px;">Kapasitet:</span>
                <span style="color: #374151; font-size: 14px;">${shelter.capacity} personer</span>
              </div>
            </div>

            <!-- Directions button -->
            <a class="directions-btn" href="${getGoogleMapsUrl(shelter.position[0], shelter.position[1])}" target="_blank" style="display: inline-flex; align-items: center; justify-content: center; background-color: #10B981; color: white; font-weight: 500; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 14px; transition: background-color 0.2s;" data-lat="${shelter.position[0]}" data-lng="${shelter.position[1]}">
              <svg xmlns="http://www.w3.org/2000/svg" style="width: 16px; height: 16px; margin-right: 6px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="3 11 22 2 13 21 11 13 3 11"></polygon>
              </svg>
              Få veibeskrivelse
            </a>
          </div>
        </div>
      `)

    markers.push(marker)
  }
}

onMounted(() => {
  if (props.map) {
    addShelters()
  }
})

watch(
  () => props.map,
  (newMap) => {
    if (newMap) {
      addShelters()
    }
  },
)
</script>

<template>
  <div></div>
</template>
