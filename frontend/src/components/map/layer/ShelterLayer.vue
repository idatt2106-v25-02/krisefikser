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
    'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48cGF0aCBkPSJNMyA5bDkgLTcgOSA3djExYTIgMiAwIDAgMSAtMiAyaC0xNGEyIDIgMCAwIDEgLTIgLTJ6Ij48L3BhdGg+PHBvbHlsaW5lIHBvaW50cz0iOSAyMiA5IDEyIDE1IDEyIDE1IDIyIj48L3BvbHlsaW5lPjwvc3ZnPg==',
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

