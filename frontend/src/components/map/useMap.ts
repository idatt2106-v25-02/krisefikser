import { ref } from 'vue'
import L from 'leaflet'

const TRONDHEIM_CENTER: [number, number] = [63.4305, 10.3951]

export const useMap = () => {
  const map = L.map('map').setView(TRONDHEIM_CENTER, 13)
  const markers = ref<L.Marker[]>([])

  const addMarker = (
    latitude: number,
    longitude: number,
    iconUrl: string,
    popupContent: string,
    options?: L.MarkerOptions,
  ) => {
    const marker = L.marker([latitude, longitude], {
      icon: L.icon({
        iconUrl: iconUrl,
      }),
      ...options,
    })
      .addTo(map)
      .bindPopup(popupContent)

    markers.value.push(marker)
  }

  return { map, markers, addMarker }
}
