import { type MarkerComponent, MarkerType } from '.'

export async function createUserMarker(): Promise<MarkerComponent> {
  const markerPosition = await new Promise<{ latitude: number; longitude: number }>(
    (resolve, _reject) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          })
        },
        (error) => {
          console.error('Error getting user location:', error)
          // Fallback to default position if geolocation fails
          resolve({
            latitude: 63.4305,
            longitude: 10.3951,
          })
        },
      )
    },
  )

  return {
    latitude: markerPosition.latitude,
    longitude: markerPosition.longitude,
    iconUrl: '/icons/map/user.svg',
    popupContent: 'User Location',
    type: MarkerType.User,
  }
}
