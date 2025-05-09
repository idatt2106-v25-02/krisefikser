<script setup lang="ts">
import { useGetActiveHousehold } from '@/api/generated/household/household'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { createUserMarker } from '@/components/map/marker'
import { createHouseholdMarker } from '@/components/map/marker/household'
import { createHouseholdMemberMarker } from '@/components/map/marker/householdMember'
import loadMapPoints from '@/components/map/marker/mapPoints'
import NewMapComponent from '@/components/map/NewMapComponent.vue'
import { useMap } from '@/components/map/useMap'
import type { Map as LeafletMap } from 'leaflet'
import { computed, watch } from 'vue'

const { data: mapPointsData, isLoading: isLoadingMapPoints } = useGetAllMapPoints()
const { data: mapPointTypesData, isLoading: isLoadingMapPointTypes } = useGetAllMapPointTypes()
const { data: activeHousehold, isLoading: isLoadingActiveHousehold } = useGetActiveHousehold()

const { initMap, addMarkers, onMapCreated, clearMarkers } = useMap()

const isDataLoading = computed(() => {
  return isLoadingMapPoints.value || isLoadingMapPointTypes.value || isLoadingActiveHousehold.value
})

const renderNewMapPoints = async () => {
  clearMarkers()

  // Shelter and other map points (points added through admin dashboard)
  if (mapPointsData.value && mapPointTypesData.value) {
    const mapMarkers = loadMapPoints(mapPointsData.value, mapPointTypesData.value)
    addMarkers(mapMarkers)
  }

  // Household realted markers
  if (activeHousehold.value) {
    // Household marker
    const householdMarker = await createHouseholdMarker(activeHousehold.value)
    addMarkers([householdMarker])

    // Householdmembers markers
    const householdMembers = activeHousehold.value.members
    const householdMembersMarkers = householdMembers
      .map((member) => createHouseholdMemberMarker(member))
      .filter((marker) => marker !== null)
    addMarkers(householdMembersMarkers)
  }

  // User location marker
  // const userMarker = await createUserMarker()
  // addMarkers([userMarker])
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
