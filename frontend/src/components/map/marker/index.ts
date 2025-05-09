import { createUserMarker } from './user'

interface MarkerComponent {
  latitude: number
  longitude: number
  iconUrl: string
  popupContent: string
  options?: L.MarkerOptions
  type: MarkerType
}

enum MarkerType {
  Home = 'home',
  User = 'user',
  Event = 'event',
  Shelter = 'shelter',
  MeetingPoint = 'meetingPoint',
  HouseholdMember = 'householdMember',
  Other = 'other',
}

const MARKER_ICONS = {
  shelter: '/icons/map/shelter.svg',
  user: '/icons/map/user.svg',
  home: '/icons/map/home.svg',
  household_members: '/icons/map/household-members.svg',
  meeting_point: '/icons/map/meeting-point.svg',
}

export type { MarkerComponent }
export { MarkerType, createUserMarker, MARKER_ICONS }
