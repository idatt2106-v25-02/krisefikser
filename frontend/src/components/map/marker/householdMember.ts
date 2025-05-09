import type { HouseholdMemberResponse } from '@/api/generated/model'
import { MarkerType, type MarkerComponent } from '.'

export function createHouseholdMemberMarker(
  member: HouseholdMemberResponse,
): MarkerComponent | null {
  if (!member.user.latitude || !member.user.longitude) {
    return null
  }

  return {
    latitude: member.user.latitude,
    longitude: member.user.longitude,
    iconDiv: createHouseholdMemberIconDiv(member),
    popupContent: member.user.firstName + ' ' + member.user.lastName,
    type: MarkerType.HouseholdMember,
  }
}

export function createHouseholdMemberIconDiv(member: HouseholdMemberResponse): string {
  const initials =
    (member.user.firstName?.charAt(0) ?? '') + (member.user.lastName?.charAt(0) ?? '')

  const SIZE = 36

  return `
  <div style="
    position: relative;
    width: ${SIZE}px;
    height: ${SIZE}px;
    border-radius: 50%;
    background-color: #DCE5F9;
    border: 3px solid #2563EB;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #2563EB;
    font-size: ${SIZE / 2.5}px;
    font-weight: bold;
    box-sizing: border-box;
    transform: translate(-35%, 10%);
  ">
    ${initials}
  </div>
  `
}
