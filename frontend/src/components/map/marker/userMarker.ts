import type { MarkerComponent } from '.'

export function createUserMarker(): MarkerComponent {
  let markerPosition = {
    latitude: 63.4305,
    longitude: 10.3951,
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      markerPosition = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      }
      console.log('markerPosition', markerPosition)
    },
    (error) => {
      console.error('Error getting user location:', error)
    },
  )

  return {
    latitude: markerPosition.latitude,
    longitude: markerPosition.longitude,
    iconUrl: '/icons/map/user.svg',
    popupContent: 'User Location',
  }
}
