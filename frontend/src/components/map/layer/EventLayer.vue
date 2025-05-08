<script lang="ts" setup>
import { onMounted, watch } from 'vue'
import L from 'leaflet'
import type { EventResponse } from '@/api/generated/model'
import {
  EventResponseLevel as EventLevel,
  EventResponseStatus as EventStatus,
} from '@/api/generated/model'
import { webSocket } from '@/main.ts'

// Define props
const props = defineProps<{
  map: L.Map | null
  events: EventResponse[]
}>()

const circleMap = new Map<number, L.Circle>()

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

// Create popup content
function createPopupContent(event: EventResponse, color: string): string {
  const startTime = event.startTime
    ? new Date(event.startTime).toLocaleString()
    : 'Ikke tilgjengelig'
  const endTime = event.endTime ? new Date(event.endTime).toLocaleString() : 'Ikke tilgjengelig'

  const statusText = event.status
    ? `<span style="font-weight: bold; color: ${color}">Status: ${event.status}</span>`
    : ''

  return `
    <div style="min-width: 200px">
      <h3 style="margin: 0 0 8px; color: ${color}">${event.title || 'Hendelse uten navn'}</h3>
      ${statusText}<br>
      <p style="margin: 8px 0">${event.description || 'Ingen beskrivelse tilgjengelig'}</p>
      <div style="margin-top: 8px">
        <div><strong>Start:</strong> ${startTime}</div>
        ${event.endTime ? `<div><strong>Slutt:</strong> ${endTime}</div>` : ''}
      </div>
    </div>
  `
}

// Add a single event to the map
function addEvent(event: EventResponse) {
  if (!props.map || !event.id || !event.latitude || !event.longitude || !event.radius) {
    return
  }

  // Skip if this event is already on the map
  if (circleMap.has(event.id)) {
    updateEvent(event)
    return
  }

  const color = getEventColor(event.level)
  const opacity = getEventOpacity(event.status)

  const circle = L.circle([event.latitude, event.longitude], {
    radius: event.radius,
    color: color,
    fillColor: color,
    fillOpacity: opacity,
    weight: 2,
  }).addTo(props.map)

  circle.bindPopup(createPopupContent(event, color))

  // Store the circle for later reference
  circleMap.set(event.id, circle)
}

// Update an existing event circle
function updateEvent(event: EventResponse) {
  if (!event.id || !event.latitude || !event.longitude || !event.radius) {
    return
  }

  const circle = circleMap.get(event.id)
  if (!circle) {
    // If we don't have this circle yet, add it
    addEvent(event)
    return
  }

  const color = getEventColor(event.level)
  const opacity = getEventOpacity(event.status)

  // Update circle properties
  circle.setLatLng([event.latitude, event.longitude])
  circle.setRadius(event.radius)
  circle.setStyle({
    color: color,
    fillColor: color,
    fillOpacity: opacity,
    weight: 2,
  })

  // Update popup content
  circle.unbindPopup()
  circle.bindPopup(createPopupContent(event, color))
}

// Remove an event from the map
function removeEvent(eventId: number) {
  const circle = circleMap.get(eventId)
  if (circle && props.map) {
    circle.remove()
    circleMap.delete(eventId)
  }
}

// Initialize events
function initializeEvents() {
  if (!props.map) return
  circleMap.clear()
  // Add all initial events to the map
  for (const event of props.events) {
    if (event.id) {
      addEvent(event)
    }
  }
}

// Handler for new events coming from WebSocket
function initializeWebsocket() {
  webSocket.subscribe<EventResponse>('/topic/events', (event) => {
    updateEvent(event)
  })

  webSocket.subscribe<EventResponse>('/topic/events/new', (event) => {
    addEvent(event)
  })

  webSocket.subscribe<number>('/topic/events/delete', (eventId) => {
    removeEvent(eventId)
  })
}

onMounted(() => {
  initializeEvents()
  initializeWebsocket()
})

watch(
  () => props.map,
  (newMap) => {
    if (newMap) {
      initializeEvents()
    }
  },
)

watch(
  () => props.events,
  () => {
    initializeEvents()
  },
  { deep: true },
)
</script>

<template>
  <div></div>
</template>
