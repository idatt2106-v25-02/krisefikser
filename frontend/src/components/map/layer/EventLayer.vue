<script setup lang="ts">
import { onMounted, watch } from 'vue'
import L from 'leaflet'
import type { EventResponse } from '@/api/generated/model'
import {
  EventResponseLevel as EventLevel,
  EventResponseStatus as EventStatus,
} from '@/api/generated/model'

// Define props
const props = defineProps<{
  map: L.Map | null
  events: EventResponse[]
}>()

const circles: L.Circle[] = []

// Helper function to determine color based on event level
function getEventColor(level?: EventLevel): string {
  switch (level) {
    case EventLevel.RED:
      return '#F44336' // Red
    case EventLevel.YELLOW:
      return '#FFC107' // Yellow
    case EventLevel.GREEN:
    default:
      return '#4CAF50' // Green
  }
}

// Helper function to determine opacity based on event status
function getEventOpacity(status?: EventStatus): number {
  switch (status) {
    case EventStatus.UPCOMING:
      return 0.3
    case EventStatus.ONGOING:
      return 0.6
    case EventStatus.FINISHED:
      return 0.1
    default:
      return 0.5
  }
}

// Add events to map
function addEvents() {
  if (!props.map) return

  // Clear previous circles
  for (const circle of circles) {
    circle.remove()
  }
  circles.length = 0

  // Add events
  for (const event of props.events) {
    if (event.latitude && event.longitude && event.radius) {
      const color = getEventColor(event.level)
      const opacity = getEventOpacity(event.status)

      const startTime = event.startTime
        ? new Date(event.startTime).toLocaleString()
        : 'Ikke tilgjengelig'
      const endTime = event.endTime ? new Date(event.endTime).toLocaleString() : 'Ikke tilgjengelig'

      const statusText = event.status
        ? `<span style="font-weight: bold; color: ${color}">Status: ${event.status}</span>`
        : ''

      const circle = L.circle([event.latitude, event.longitude], {
        radius: event.radius,
        color: color,
        fillColor: color,
        fillOpacity: opacity,
        weight: 2,
      }).addTo(props.map).bindPopup(`
          <div style="min-width: 200px">
            <h3 style="margin: 0 0 8px; color: ${color}">${event.title || 'Hendelse uten navn'}</h3>
            ${statusText}<br>
            <p style="margin: 8px 0">${event.description || 'Ingen beskrivelse tilgjengelig'}</p>
            <div style="margin-top: 8px">
              <div><strong>Start:</strong> ${startTime}</div>
              ${event.endTime ? `<div><strong>Slutt:</strong> ${endTime}</div>` : ''}
            </div>
          </div>
        `)

      circles.push(circle)
    }
  }
}

onMounted(() => {
  if (props.map) {
    addEvents()
  }
})

watch(
  () => props.map,
  (newMap) => {
    if (newMap) {
      addEvents()
    }
  },
)

watch(
  () => props.events,
  () => {
    addEvents()
  },
  { deep: true },
)
</script>
