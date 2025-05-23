import type { EventResponse } from '@/api/generated/model'
import { EventResponseLevel, EventResponseStatus } from '@/api/generated/model'
import { MarkerType, type MarkerComponent } from '.'
import { formatDate } from '@/utils/date-formatter'

export function createEventMarkers(events: EventResponse[]): MarkerComponent[] {
  return events
    .filter((event) => event.latitude && event.longitude && event.radius)
    .map((event) => ({
      latitude: event.latitude,
      longitude: event.longitude,
      radius: event.radius,
      color: getEventColor(event.level, event.status),
      popupContent: createEventPopupContent(event),
      type: MarkerType.Event,
      isCircle: true,
      circleOptions: {
        radius: event.radius,
        color: getEventColor(event.level, event.status),
        fillColor: getEventColor(event.level, event.status),
        fillOpacity: 0.3,
        weight: 2,
      },
    }))
}

// Helper function to get severity name based on event level
function getSeverityName(level?: EventResponseLevel): string {
  switch (level) {
    case EventResponseLevel.RED:
      return 'Kritisk'
    case EventResponseLevel.YELLOW:
      return 'Moderat'
    case EventResponseLevel.GREEN:
      return 'Lav'
    default:
      return 'Ukjent'
  }
}

// Helper function to get status name based on event status
function getStatusName(status?: EventResponseStatus): string {
  switch (status) {
    case EventResponseStatus.ONGOING:
      return 'Pågående'
    case EventResponseStatus.UPCOMING:
      return 'Kommende'
    default:
      return 'Ukjent'
  }
}

// Helper function to determine color based on event level and status
function getEventColor(level?: EventResponseLevel, status?: EventResponseStatus): string {
  // Color combinations based on both level and status
  if (status === EventResponseStatus.ONGOING) {
    switch (level) {
      case EventResponseLevel.RED:
        return '#DC2626' // Tailwind red-600
      case EventResponseLevel.YELLOW:
        return '#FBBF24' // Tailwind amber-400
      case EventResponseLevel.GREEN:
      default:
        return '#10B981' // Tailwind emerald-500
    }
  }

  switch (level) {
    case EventResponseLevel.RED:
      return '#EF4444' // Tailwind red-500
    case EventResponseLevel.YELLOW:
      return '#F59E0B' // Tailwind amber-500
    case EventResponseLevel.GREEN:
    default:
      return '#34D399' // Tailwind emerald-400
  }
}

// Create popup content
function createEventPopupContent(event: EventResponse): string {
  const startTime = formatDate(event.startTime)
  const endTime = event.endTime ? formatDate(event.endTime) : undefined
  const color = getEventColor(event.level, event.status)

  return `
    <div style="min-width: 300px; font-family: system-ui, -apple-system, sans-serif; border-radius: 8px; overflow: hidden;">
      <div style="padding: 16px;">
        <h3 style="margin: 0 0 12px; color: #1f2937; font-size: 18px; font-weight: 700;">${event.title || 'Hendelse uten navn'}</h3>

        <!-- Metadata badges -->
        <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px;">
          <span style="display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 500; background-color: ${color + '20'}; color: ${color};">
            <svg xmlns="http://www.w3.org/2000/svg" style="height: 12px; width: 12px; margin-right: 4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M13 10V3L4 14h7v7l9-11h-7z"></path>
            </svg>
            ${getStatusName(event.status)}
          </span>

          <span style="display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 500; background-color: ${color + '10'}; color: ${color};">
            <svg xmlns="http://www.w3.org/2000/svg" style="height: 12px; width: 12px; margin-right: 4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            ${getSeverityName(event.level)}
          </span>
        </div>

        <!-- Event description -->
        <p style="margin: 0 0 16px; color: #4b5563; line-height: 1.5; font-size: 14px;">${event.description || 'Ingen beskrivelse tilgjengelig'}</p>

        <!-- Time information -->
        <div style="display: flex; flex-direction: column; gap: 4px; background: #f9fafb; padding: 10px; border-radius: 6px; margin-bottom: 16px;">
          <div style="display: flex; align-items: center;">
            <svg xmlns="http://www.w3.org/2000/svg" style="height: 14px; width: 14px; margin-right: 6px; color: #6b7280;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span style="font-weight: 500; width: 60px; color: #6b7280; font-size: 14px;">Start:</span>
            <span style="color: #374151; font-size: 14px;">${startTime}</span>
          </div>
          ${
            endTime
              ? `<div style="display: flex; align-items: center;">
                <svg xmlns="http://www.w3.org/2000/svg" style="height: 14px; width: 14px; margin-right: 6px; color: #6b7280;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                  <line x1="16" y1="2" x2="16" y2="6"></line>
                  <line x1="8" y1="2" x2="8" y2="6"></line>
                  <line x1="3" y1="10" x2="21" y2="10"></line>
                </svg>
                <span style="font-weight: 500; width: 60px; color: #6b7280; font-size: 14px;">Slutt:</span>
                <span style="color: #374151; font-size: 14px;">${endTime}</span>
              </div>`
              : ''
          }
        </div>

        <!-- Navigation button -->
        <a href="/kriser/${event.id}" style="display: inline-flex; align-items: center; justify-content: center; background-color: #3b82f6; color: white; font-weight: 500; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 14px; transition: background-color 0.2s;">
          <svg xmlns="http://www.w3.org/2000/svg" style="width: 16px; height: 16px; margin-right: 6px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
            <polyline points="15 3 21 3 21 9"></polyline>
            <line x1="10" y1="14" x2="21" y2="3"></line>
          </svg>
          Se detaljer
        </a>
      </div>
    </div>
  `
}
