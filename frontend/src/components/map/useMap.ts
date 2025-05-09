import { ref } from 'vue'
import L from 'leaflet'
import { setupCustomControls } from './controls'
import type { MarkerComponent } from './marker'

const TRONDHEIM_CENTER: [number, number] = [63.4305, 10.3951]
const INITIAL_ZOOM = 13

interface MarkerInstance {
  leafletMarker: L.Marker | L.Circle | null
  markerComponent: MarkerComponent
}

export interface FilterOptions {
  eventEnabled: boolean
  shelterEnabled: boolean
  meetingPointEnabled: boolean
  householdMemberEnabled: boolean
  homeEnabled: boolean
  userEnabled: boolean
  otherEnabled: boolean
}

function addTileLayer(map: L.Map) {
  if (import.meta.env.VITE_MAPBOX_ACCESS_TOKEN) {
    L.tileLayer(
      'https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}',
      {
        id: 'mapbox/streets-v12',
        accessToken: import.meta.env.VITE_MAPBOX_ACCESS_TOKEN,
        tileSize: 512,
        zoomOffset: -1,
      } as L.TileLayerOptions & { accessToken: string },
    ).addTo(map)
  } else {
    // Add OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map)
  }
}

function createMarker(markerComponent: MarkerComponent, map: L.Map): L.Marker | L.Circle {
  if (markerComponent.isCircle && markerComponent.circleOptions) {
    // Create a circle marker for events
    return L.circle(
      [markerComponent.latitude, markerComponent.longitude],
      markerComponent.circleOptions,
    )
      .addTo(map)
      .bindPopup(markerComponent.popupContent)
  } else {
    // Create a regular marker
    const markerOptions: L.MarkerOptions = {}

    // Set icon if available
    if (markerComponent.iconUrl) {
      markerOptions.icon = L.icon({
        iconUrl: markerComponent.iconUrl,
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32],
      })
    } else if (markerComponent.iconDiv) {
      markerOptions.icon = L.divIcon({
        className: 'marker-icon',
        html: markerComponent.iconDiv,
      })
    }

    // Merge with any additional options
    if (markerComponent.options) {
      Object.assign(markerOptions, markerComponent.options)
    }

    return L.marker([markerComponent.latitude, markerComponent.longitude], markerOptions)
      .addTo(map)
      .bindPopup(markerComponent.popupContent)
  }
}

export const useMap = () => {
  const map = ref<L.Map | null>(null)
  const markers = ref<MarkerInstance[]>([])

  const initMap = (elementId: string = 'map', callback?: () => void) => {
    if (map.value) {
      console.warn('Map is already initialized.')
      return
    }

    map.value = L.map(elementId, { zoomControl: false }).setView(TRONDHEIM_CENTER, INITIAL_ZOOM)
    addTileLayer(map.value as L.Map)
    setupCustomControls(map.value as L.Map, TRONDHEIM_CENTER, INITIAL_ZOOM)
    callback?.()
  }

  const addMarkers = (markerComponents: MarkerComponent[]) => {
    if (!map.value) {
      throw new Error('Map is not initialized')
    }

    markerComponents.forEach((markerComponent) => {
      const leafletMarker = createMarker(markerComponent, map.value as L.Map)
      markers.value.push({ leafletMarker, markerComponent })
    })
  }

  const clearMarkers = () => {
    if (map.value) {
      markers.value.forEach(({ leafletMarker }) => {
        leafletMarker?.remove()
      })
    }
    markers.value = []
  }

  const filterMarkers = (options: FilterOptions) => {
    if (!map.value) return

    markers.value.forEach((marker) => {
      if (isFiltered(marker.markerComponent as MarkerComponent, options)) {
        marker.leafletMarker?.remove()
        marker.leafletMarker = null
      } else {
        marker.leafletMarker = createMarker(
          marker.markerComponent as MarkerComponent,
          map.value as L.Map,
        )
      }
    })
  }

  return { map, initMap, markers, addMarkers, clearMarkers, filterMarkers }
}

function isFiltered(markerComponent: MarkerComponent, options: FilterOptions) {
  switch (markerComponent.type) {
    case 'event':
      return options.eventEnabled
    case 'shelter':
      return options.shelterEnabled
    case 'meetingPoint':
      return options.meetingPointEnabled
    case 'householdMember':
      return options.householdMemberEnabled
    case 'home':
      return options.homeEnabled
    case 'user':
      return options.userEnabled
    default:
      return options.otherEnabled
  }
}
