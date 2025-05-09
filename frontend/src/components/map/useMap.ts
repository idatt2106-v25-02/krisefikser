import { ref } from 'vue'
import L from 'leaflet'

const TRONDHEIM_CENTER: [number, number] = [63.4305, 10.3951]

type MapCreatedCallback = (map: L.Map) => void

export const useMap = () => {
  const map = ref<L.Map | null>(null)
  const markers = ref<L.Marker[]>([])
  const mapCreatedCallbacks = ref<MapCreatedCallback[]>([])

  const initMap = (elementId: string = 'map') => {
    if (map.value) {
      console.warn('Map is already initialized.');
      return
    }

    map.value = L.map(elementId).setView(TRONDHEIM_CENTER, 13)
    mapCreatedCallbacks.value.forEach((callback) => callback(map.value as L.Map))
  }

  const onMapCreated = (callback: MapCreatedCallback) => {
    if (map.value) {
      callback(map.value as L.Map)
    } else {
      mapCreatedCallbacks.value.push(callback)
    }
  }

  const addMarker = (
    latitude: number,
    longitude: number,
    iconUrl: string,
    popupContent: string,
    options?: L.MarkerOptions,
  ) => {
    if (!map.value) {
      throw new Error('Map is not initialized')
    }

    const marker = L.marker([latitude, longitude], {
      icon: L.icon({
        iconUrl: iconUrl,
      }),
      ...options,
    })
      .addTo(map.value as L.Map)
      .bindPopup(popupContent)

    markers.value.push(marker)
  }

  return { map, initMap, markers, addMarker, onMapCreated }
}
