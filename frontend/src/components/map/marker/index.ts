interface MarkerComponent {
  latitude: number
  longitude: number
  iconUrl: string
  popupContent: string
  options?: L.MarkerOptions
}

enum MarkerType {
  Home = 'home',
  User = 'user',
  Event = 'event',
  Shelter = 'shelter',
  MeetingPoint = 'meetingPoint',
  HouseholdMember = 'householdMember',
}

export type { MarkerComponent }
export { MarkerType }
