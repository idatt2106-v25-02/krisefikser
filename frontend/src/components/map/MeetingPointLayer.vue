<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'
import L from 'leaflet'
import { useGetMeetingPoints } from '@/api/generated/meeting-points/meeting-points'
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
    className: 'meeting-point-marker',
    html: '<div class="w-4 h-4 bg-blue-500 rounded-full border-2 border-white"></div>',
    iconSize: [16, 16],
    iconAnchor: [8, 8]
  })
}

// Update markers when meeting points change
watch(meetingPointsData, (newPoints) => {
  if (!newPoints) return

  // Clear existing markers
  markers.value.forEach(marker => marker.remove())
  markers.value = []

  // Create new markers
  meetingPoints.value = Array.isArray(newPoints) ? newPoints : [newPoints]
  meetingPoints.value.forEach(point => {
    if (!point.latitude || !point.longitude) return

    const marker = L.marker([point.latitude, point.longitude], {
      icon: createMarkerIcon()
    })

    marker.bindPopup(`
      <div class="p-2">
        <h3 class="font-bold">${point.name}</h3>
        <p>${point.description}</p>
      </div>
    `)

    marker.on('click', () => {
      emit('meeting-point-clicked', point)
    })

    marker.addTo(props.map!)
    markers.value.push(marker)
  })
})

// Clean up markers when component is unmounted
onUnmounted(() => {
  markers.value.forEach(marker => marker.remove())
  markers.value = []
})
</script>

<template>
  <div v-if="isLoading" class="absolute top-0 left-0 right-0 bg-slate-700 text-white p-2 text-center">
    Laster møtepunkter...
  </div>
</template>

<style scoped>
.meeting-point-marker {
  filter: drop-shadow(0 0 2px rgba(0, 0, 0, 0.5));
}
</style>
