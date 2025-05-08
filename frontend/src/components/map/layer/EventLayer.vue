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

// Helper function to get severity name based on event level
function getSeverityName(level?: EventLevel): string {
  switch (level) {
    case EventLevel.RED:
      return 'Kritisk'
    case EventLevel.YELLOW:
      return 'Moderat'
    case EventLevel.GREEN:
      return 'Lav'
    default:
      return 'Ukjent'
  }
}

// Helper function to get status name based on event status
function getStatusName(status?: EventStatus): string {
  switch (status) {
    case EventStatus.ONGOING:
      return 'Pågående'
    case EventStatus.UPCOMING:
      return 'Kommende'
    default:
      return 'Ukjent'
  }
}

// Helper function to determine color based on event level and status
function getEventColor(level?: EventLevel, status?: EventStatus): string {
  // Color combinations based on both level and status
  if (status === EventStatus.ONGOING) {
    switch (level) {
      case EventLevel.RED:
        return '#DC2626' // Tailwind red-600
      case EventLevel.YELLOW:
        return '#FBBF24' // Tailwind amber-400
      case EventLevel.GREEN:
      default:
        return '#10B981' // Tailwind emerald-500
    }
  }

  switch (level) {
    case EventLevel.RED:
      return '#EF4444' // Tailwind red-500
    case EventLevel.YELLOW:
      return '#F59E0B' // Tailwind amber-500
    case EventLevel.GREEN:
    default:
      return '#34D399' // Tailwind emerald-400
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
      const color = getEventColor(event.level, event.status)

      const startTime = event.startTime
        ? new Date(event.startTime).toLocaleString()
        : 'Ikke tilgjengelig'
      const endTime = event.endTime ? new Date(event.endTime).toLocaleString() : 'Ikke tilgjengelig'

      const circle = L.circle([event.latitude, event.longitude], {
        radius: event.radius,
        color: color,
        fillColor: color,
        fillOpacity: 0.4,
        weight: 2,
      }).addTo(props.map).bindPopup(`
          <div style="min-width: 250px; font-family: system-ui, -apple-system, sans-serif; border-radius: 8px; overflow: hidden;">
            <div style="padding: 12px 16px; background: ${color}15;">
              <h3 style="margin: 0; color: ${color}; font-size: 18px;">${event.title || 'Hendelse uten navn'}</h3>
              <div style="display: flex; gap: 6px; margin-top: 8px;">
                <div style="display: inline-block; padding: 4px 8px; border-radius: 4px; background-color: ${color}; color: white; font-weight: 600; font-size: 12px; text-transform: uppercase;">
                  ${getStatusName(event.status)}
                </div>
                <div style="display: inline-block; padding: 4px 8px; border-radius: 4px; background-color: ${color}40; color: ${color}; font-weight: 600; font-size: 12px; text-transform: uppercase;">
                  ${getSeverityName(event.level)}
                </div>
              </div>
            </div>
            <div style="padding: 16px;">
              <p style="margin: 0 0 16px; line-height: 1.5;">${event.description || 'Ingen beskrivelse tilgjengelig'}</p>
              <div style="display: flex; flex-direction: column; gap: 4px; background: #f5f5f5; padding: 10px; border-radius: 6px;">
                <div style="display: flex; align-items: center;">
                  <span style="font-weight: 600; width: 60px;">Start:</span>
                  <span>${startTime}</span>
                </div>
                ${
                  event.endTime
                    ? `<div style="display: flex; align-items: center;">
                  <span style="font-weight: 600; width: 60px;">Slutt:</span>
                  <span>${endTime}</span>
                </div>`
                    : ''
                }
              </div>
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
