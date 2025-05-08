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

// Helper function to determine color based on event level and status
function getEventColor(level?: EventLevel, status?: EventStatus): string {
  // Color combinations based on both level and status
  if (status === EventStatus.ONGOING) {
    switch (level) {
      case EventLevel.RED:
        return '#FF3B30' // Vibrant red for ongoing red events
      case EventLevel.YELLOW:
        return '#FFCC00' // Bright yellow for ongoing yellow events
      case EventLevel.GREEN:
      default:
        return '#34C759' // Bright green for ongoing green events
    }
  }

  switch (level) {
    case EventLevel.RED:
      return '#FF9500' // Orange for upcoming red events
    case EventLevel.YELLOW:
      return '#FFD60A' // Light yellow for upcoming yellow events
    case EventLevel.GREEN:
    default:
      return '#30D158' // Lighter green for upcoming green events
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
              <div style="display: inline-block; margin-top: 8px; padding: 4px 8px; border-radius: 4px; background-color: ${color}; color: white; font-weight: 600; font-size: 12px; text-transform: uppercase;">${event.status || 'Ukjent status'}</div>
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
