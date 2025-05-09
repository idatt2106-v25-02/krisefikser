import { createUserMarker } from './user'
import { createEventMarkers } from './event'
import type { CircleMarkerOptions, MarkerOptions } from 'leaflet'

interface MarkerComponent {
  latitude: number
  longitude: number
  iconUrl?: string
  iconDiv?: string
  popupContent: string
  options?: MarkerOptions
  type: MarkerType
  isCircle?: boolean
  circleOptions?: CircleMarkerOptions
  radius?: number
  color?: string
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
export { MarkerType, createUserMarker, createEventMarkers, MARKER_ICONS }
