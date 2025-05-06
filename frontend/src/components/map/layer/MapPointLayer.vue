<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import L from 'leaflet'
import type { MapPointResponse as MapPoint } from '@/api/generated/model'

const props = defineProps<{
  map: L.Map | null
  mapPoints: MapPoint[]
}>()

let markers: L.Marker[] = []

function createMarker(point: MapPoint) {
  if (!point.latitude || !point.longitude) return null

  const icon = L.icon({
    iconUrl: point.type?.iconUrl || '/default-marker.png',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  })

  const marker = L.marker([point.latitude, point.longitude], { icon })
  marker.bindPopup(`
    <div class="p-2">
      <h3 class="font-bold">${point.type?.title || 'Unknown'}</h3>
      <p class="text-sm">${point.type?.description || ''}</p>
      ${point.type?.openingTime ? `<p class="text-sm">Åpningstid: ${point.type.openingTime}</p>` : ''}
    </div>
  `)
  return marker
}

function updateMarkers() {
  if (!props.map) return

  // Remove existing markers
  markers.forEach((marker) => marker.remove())
  markers = []

  // Add new markers
  props.mapPoints.forEach((point) => {
    const marker = createMarker(point)
    if (marker) {
      marker.addTo(props.map!)
      markers.push(marker)
    }
  })
}

watch(() => props.mapPoints, updateMarkers, { deep: true })
watch(() => props.map, updateMarkers)

onMounted(updateMarkers)
onUnmounted(() => {
  markers.forEach((marker) => marker.remove())
})
</script>

<template>
  <div></div>
</template>
