<script setup lang="ts">
import { useGetAllEvents } from '@/api/generated/event/event'
import { useGetActiveHousehold } from '@/api/generated/household/household'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useGetMeetingPoints } from '@/api/generated/meeting-points/meeting-points'
import { createUserMarker } from '@/components/map/marker'
import { createHouseholdMarker } from '@/components/map/marker/household'
import { createHouseholdMemberMarker } from '@/components/map/marker/householdMember'
import loadMapPoints from '@/components/map/marker/mapPoints'
import { createMeetingPointMarker } from '@/components/map/marker/meetingPoint'
import NewMapComponent from '@/components/map/NewMapComponent.vue'
import { useMap } from '@/components/map/useMap'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import type { Map as LeafletMap } from 'leaflet'
import { computed, watch } from 'vue'

const { data: mapPointsData, isLoading: isLoadingMapPoints } = useGetAllMapPoints()
const { data: mapPointTypesData, isLoading: isLoadingMapPointTypes } = useGetAllMapPointTypes()
const { data: activeHousehold, isLoading: isLoadingActiveHousehold } = useGetActiveHousehold()
const { data: eventPointsData, isLoading: isLoadingEventPoints } = useGetAllEvents()

const authStore = useAuthStore()

const { initMap, addMarkers, onMapCreated, clearMarkers } = useMap()

const isDataLoading = computed(() => {
  return (
    isLoadingMapPoints.value ||
    isLoadingMapPointTypes.value ||
    isLoadingActiveHousehold.value ||
    isLoadingEventPoints.value
  )
})

const renderNewMapPoints = async () => {
  clearMarkers()

  // Shelter and other map points (points added through admin dashboard)
  if (mapPointsData.value && mapPointTypesData.value) {
    const mapMarkers = loadMapPoints(mapPointsData.value, mapPointTypesData.value)
    addMarkers(mapMarkers)
  }

  // Event points
  if (eventPointsData.value) {
    const eventMarkers = createEventMarkers(eventPointsData.value)
    addMarkers(eventMarkers)
  }

  // Household realted markers
  if (activeHousehold.value) {
    // Household marker
    const householdMarker = await createHouseholdMarker(activeHousehold.value)
    addMarkers([householdMarker])

    // Householdmembers markers
    const householdMembers = activeHousehold.value.members
    const householdMembersMarkers = householdMembers
      .filter((member) => member.user.id !== authStore.currentUser?.id)
      .map((member) => createHouseholdMemberMarker(member))
      .filter((marker) => marker !== null)
    addMarkers(householdMembersMarkers)

    // Meeting points markers
    const { data: meetingPointsData, isLoading: isLoadingMeetingPoints } = useGetMeetingPoints(
      activeHousehold.value.id,
    )

    watch(isLoadingMeetingPoints, () => {
      if (meetingPointsData.value) {
        const meetingPointsMarkers = meetingPointsData.value
          .map((meetingPoint) => createMeetingPointMarker(meetingPoint))
          .filter((marker) => marker !== null)
        addMarkers(meetingPointsMarkers)
      }
    })
  }

  // User location marker
  const userMarker = await createUserMarker()
  addMarkers([userMarker])
}

onMapCreated(async (_mapInstance: LeafletMap) => {
  watch(isDataLoading, renderNewMapPoints)
})
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
    <div class="relative flex-grow overflow-hidden">
      <NewMapComponent :init-map="initMap" />
    </div>
  </div>
</template>
