import { ref } from 'vue'
import L from 'leaflet'
import { setupCustomControls } from './controls'
import type { MarkerComponent } from './marker'

const TRONDHEIM_CENTER: [number, number] = [63.4305, 10.3951]
const INITIAL_ZOOM = 13

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

interface MarkerInstance {
  marker: L.Marker | L.Circle
  markerComponent: MarkerComponent
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
      if (markerComponent.isCircle && markerComponent.circleOptions) {
        // Create a circle marker for events
        const circle = L.circle(
          [markerComponent.latitude, markerComponent.longitude],
          markerComponent.circleOptions,
        )
          .addTo(map.value as L.Map)
          .bindPopup(markerComponent.popupContent)

        markers.value.push({ marker: circle, markerComponent })
      } else {
        // Create a regular marker
        const marker = L.marker([markerComponent.latitude, markerComponent.longitude], {
          icon: markerComponent.iconUrl
            ? L.icon({
                iconUrl: markerComponent.iconUrl,
                iconSize: [32, 32],
                iconAnchor: [16, 32],
                popupAnchor: [0, -32],
              })
            : L.divIcon({
                className: 'marker-icon',
                html: markerComponent.iconDiv,
              }),
          ...markerComponent.options,
        })
          .addTo(map.value as L.Map)
          .bindPopup(markerComponent.popupContent)

        markers.value.push({ marker, markerComponent })
      }
    })
  }

  const clearMarkers = () => {
    if (map.value) {
      markers.value.forEach(({ marker }) => {
        map.value?.removeLayer(marker as unknown as L.Layer)
      })
    }
    markers.value = []
  }

  return { map, initMap, markers, addMarkers, clearMarkers }
}
