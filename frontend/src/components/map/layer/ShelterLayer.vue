<script setup lang="ts">
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

// Add shelters to map
function addShelters() {
  if (!props.map) return

  // Clear previous markers
  markers.forEach((marker) => marker.remove())
  markers.length = 0

  // Add shelters to map
  for (const shelter of props.shelters) {
    const marker = L.marker(shelter.position, { icon: shelterIcon }).addTo(props.map).bindPopup(`
        <strong>${shelter.name}</strong><br>
        Kapasitet: ${shelter.capacity} personer<br>
        <button class="directions-btn bg-[#4CAF50] text-white border-none py-1 px-2.5 mt-1 rounded cursor-pointer hover:bg-[#45a049]" data-lat="${shelter.position[0]}" data-lng="${shelter.position[1]}">
          Få veibeskrivelse
        </button>
      `)

    // Event listener for directions button in popup
    marker.on('popupopen', () => {
      setTimeout(() => {
        const btn = document.querySelector('.directions-btn')
        if (btn) {
          btn.addEventListener('click', (e) => {
            const target = e.target as HTMLElement
            const lat = target.getAttribute('data-lat')
            const lng = target.getAttribute('data-lng')
            if (lat && lng) {
              window.open(
                `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`,
                '_blank',
              )
            }
          })
        }
      }, 100)
    })

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
  <template></template>
</template>
