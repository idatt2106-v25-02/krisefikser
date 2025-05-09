<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'
import L from 'leaflet'
import { useGetMeetingPoints } from '@/api/generated/meeting-points/meeting-points.ts'
import type { MeetingPointResponse } from '@/api/generated/model'

const props = defineProps<{
  map: L.Map | null
  householdId: string
}>()

const emit = defineEmits<(e: 'meeting-point-clicked', point: MeetingPointResponse) => void>()

const markers = ref<L.Marker[]>([])
const meetingPoints = ref<MeetingPointResponse[]>([])

// Fetch meeting points
const { data: meetingPointsData, isLoading } = useGetMeetingPoints(props.householdId)

// Create marker icon
const createMarkerIcon = () => {
  return L.divIcon({
    className: 'meeting-point-marker filter drop-shadow',
    html: `
      <div class="relative">
        <div class="absolute -bottom-1 left-1/2 transform -translate-x-1/2 bg-blue-500 text-white text-xs px-2 py-0.5 rounded-full whitespace-nowrap">
          Møtepunkt
        </div>
      </div>
    `,
    iconSize: [32, 32],
    iconAnchor: [16, 16]
  })
}

// Clear all markers
function clearMarkers() {
  markers.value.forEach(marker => marker.remove())
  markers.value = []
}

// Update markers when meeting points change
watch([meetingPointsData, () => props.map], ([newPoints, map]) => {
  if (!newPoints || !map) return

  // Clear existing markers
  clearMarkers()

  // Create new markers
  meetingPoints.value = Array.isArray(newPoints) ? newPoints : [newPoints]
  meetingPoints.value.forEach(point => {
    if (!point.latitude || !point.longitude) return

    const marker = L.marker([point.latitude, point.longitude], {
      icon: createMarkerIcon()
    })

    const popup = L.popup({
      className: 'meeting-point-popup'
    }).setContent(`
      <div class="p-2">
        <h3 class="font-bold">${point.name}</h3>
        <p>${point.description}</p>
        <button
          class="mt-2 w-full bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm"
          data-action="edit"
        >
          Rediger
        </button>
      </div>
    `)

    marker.bindPopup(popup)

    marker.on('popupopen', () => {
      const button = document.querySelector('.meeting-point-popup [data-action="edit"]')
      if (button) {
        button.addEventListener('click', () => {
          emit('meeting-point-clicked', point)
        })
      }
    })

    marker.addTo(map)
    markers.value.push(marker)
  })
}, { immediate: true })

// Clean up markers when component is unmounted
onUnmounted(() => {
  clearMarkers()
})
</script>

<template>
  <div v-if="isLoading" class="absolute top-0 left-0 right-0 bg-slate-700 text-white p-2 text-center">
    Laster møtepunkter...
  </div>
</template>
