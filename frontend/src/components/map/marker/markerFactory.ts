import L from 'leaflet'

enum MarkerType {
  Shelter = 'shelter',
  Event = 'event',
  MeetingPoint = 'meetingPoint',
}

const getMarker()

export const createMarker = (map: L.Map, position: [number, number], markerType: MarkerType) => {
  return L.marker(position, {
    icon: L.icon({
      iconUrl: marker.iconUrl,
    }),
    ...marker.options,
  }).addTo(map)
}