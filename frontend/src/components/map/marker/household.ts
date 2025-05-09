import type { HouseholdResponse } from '@/api/generated/model'
import { MarkerType, type MarkerComponent } from '.'

async function createHouseholdMarker(household: HouseholdResponse): Promise<MarkerComponent> {
  return {
    latitude: household.latitude,
    longitude: household.longitude,
    iconUrl: '/icons/map/home.svg',
    popupContent: createHouseholdPopup(household),
    type: MarkerType.Home,
  }
}

function createHouseholdPopup(household: HouseholdResponse): string {
  return `
      <div style="min-width: 300px; font-family: system-ui, -apple-system, sans-serif; border-radius: 8px; overflow: hidden;">
        <div style="padding: 16px;">
          <h3 style="margin: 0 0 12px; color: #1f2937; font-size: 18px; font-weight: 700;">Aktiv husstand</h3>

          <!-- Metadata badge -->
          <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px;">
            <span style="display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 500; background-color: #36415320; color: #364153;">
              <svg xmlns="http://www.w3.org/2000/svg" style="height: 12px; width: 12px; margin-right: 4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
              </svg>
              ${household.address}
            </span>
          </div>

          <!-- View button -->
          <a href="/husstand" style="display: inline-flex; align-items: center; justify-content: center; background-color: #10B981; color: white; font-weight: 500; padding: 8px 16px; border-radius: 6px; text-decoration: none; font-size: 14px; transition: background-color 0.2s;">
            <svg xmlns="http://www.w3.org/2000/svg" style="width: 16px; height: 16px; margin-right: 6px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14"></path>
              <path d="m12 5 7 7-7 7"></path>
            </svg>
            Gå til husstand
          </a>
        </div>
      </div>
  `
}

export { createHouseholdMarker }
