interface Marker {
  latitude: number
  longitude: number
  iconUrl: string
  popupContent: string
  options?: L.MarkerOptions
}

const shelterMarker: Marker = {
  latitude: 63.4305,
  longitude: 10.3951,
  iconUrl: '/icons/map/shelter.svg',
  popupContent: 'Shelter',
}
