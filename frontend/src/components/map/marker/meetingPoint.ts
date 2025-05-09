import type { MeetingPointResponse } from '@/api/generated/model'
import { MarkerType, type MarkerComponent } from '.'

export function createMeetingPointMarker(
  meetingPoint: MeetingPointResponse,
): MarkerComponent | null {
  if (!meetingPoint.latitude || !meetingPoint.longitude) {
    return null
  }

  return {
    latitude: meetingPoint.latitude,
    longitude: meetingPoint.longitude,
    iconUrl: 'icons/map/meeting-point.svg',
    popupContent: createMeetingPointPopup(meetingPoint),
    type: MarkerType.MeetingPoint,
  }
}

function createMeetingPointPopup(meetingPoint: MeetingPointResponse) {
  return `
      <div class="p-2">
        <h3 class="font-bold">${meetingPoint.name}</h3>
        <p>${meetingPoint.description}</p>
        <button
          class="mt-2 w-full bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm"
          data-action="edit"
        >
          Rediger
        </button>
      </div>
  `
}
