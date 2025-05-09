import type { MapPointResponse, MapPointTypeResponse } from '@/api/generated/model'
import { MarkerType, type MarkerComponent } from '.'

function mapPointsToMarkers(
  mapPointsData: MapPointResponse[],
  mapPointTypesData: MapPointTypeResponse,
): MarkerComponent[] {
  const mapPoints = Array.isArray(mapPointsData) ? mapPointsData : [mapPointsData]
  const mapPointTypes = Array.isArray(mapPointTypesData) ? mapPointTypesData : [mapPointTypesData]

  const shelterType = mapPointTypes.find((type: MapPointTypeResponse) =>
    type.title?.toLowerCase().includes('shelter'),
  )

  return mapPoints.map((point: MapPointResponse): MarkerComponent => {
    const type = point.type?.id === shelterType?.id ? MarkerType.Shelter : MarkerType.Other
    return {
      latitude: point.latitude || 0,
      longitude: point.longitude || 0,
      iconUrl: type === MarkerType.Shelter ? '/icons/map/shelter.svg' : point.type.iconUrl,
      popupContent: type === MarkerType.Shelter ? createShelterPopup(point) : point.type.title,
      type,
    }
  })
}

function createShelterPopup(shelter: MapPointResponse): string {
  const CAPACITY = 300
  return `
        <div style="min-width: 300px; font-family: system-ui, -apple-system, sans-serif; border-radius: 8px; overflow: hidden;">
          <div style="padding: 16px;">
            <h3 style="margin: 0 0 12px; color: #1f2937; font-size: 18px; font-weight: 700;">${shelter.type.title}</h3>

            <!-- Metadata badge -->
            <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px;">
              <span style="display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 500; background-color: #36415320; color: #364153;">
                <svg xmlns="http://www.w3.org/2000/svg" style="height: 12px; width: 12px; margin-right: 4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 2 2 7l10 5 10-5-10-5z"></path>
                  <path d="M2 17l10 5 10-5"></path>
                  <path d="M2 12l10 5 10-5"></path>
                </svg>
                Tilfluktsrom
              </span>
            </div>

            <!-- Capacity information -->
            <div style="display: flex; flex-direction: column; gap: 4px; background: #f9fafb; padding: 10px; border-radius: 6px; margin-bottom: 16px;">
              <div style="display: flex; align-items: center;">
                <svg xmlns="http://www.w3.org/2000/svg" style="height: 14px; width: 14px; margin-right: 6px; color: #6b7280;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
                <span style="font-weight: 500; width: 80px; color: #6b7280; font-size: 14px;">Kapasitet:</span>
                <span style="color: #374151; font-size: 14px;">${CAPACITY} personer</span>
              </div>
            </div>

            <!-- Directions button -->
            <a class="directions-btn" href="${getGoogleMapsUrl(shelter.latitude, shelter.longitude)}" target="_blank" style="display: inline-flex; align-items: center; justify-content: center; background-color: #10B981; color: white; font-weight: 500; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 14px; transition: background-color 0.2s;" data-lat="${shelter.latitude}" data-lng="${shelter.longitude}">
              <svg xmlns="http://www.w3.org/2000/svg" style="width: 16px; height: 16px; margin-right: 6px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="3 11 22 2 13 21 11 13 3 11"></polygon>
              </svg>
              Få veibeskrivelse
            </a>
          </div>
        </div>
      `
}

function getGoogleMapsUrl(lat: number, lng: number) {
  return lat && lng ? `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}` : null
}

export default mapPointsToMarkers
