<script lang="ts" setup>
import { computed, onMounted, onUnmounted, watch } from 'vue'
import NewMapLegend from '@/components/map/NewMapLegend.vue'

// Components and composables
import {
  createEventMarkers,
  createHouseholdMarker,
  createHouseholdMemberMarker,
  createMeetingPointMarker,
  createUserMarker,
  mapPointsToMarkers,
} from '@/components/map/marker'
import { useMap } from '@/components/map/useMap'

// API imports
import { useGetAllEvents } from '@/api/generated/event/event'
import { useGetActiveHousehold } from '@/api/generated/household/household'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { getMeetingPoints } from '@/api/generated/meeting-points/meeting-points'
import type { EventResponse } from '@/api/generated/model'

import { useAuthStore } from '@/stores/auth/useAuthStore'
import { webSocket } from '@/main'

const authStore = useAuthStore()

const { data: mapPointsData, isLoading: isLoadingMapPoints } = useGetAllMapPoints()
const { data: mapPointTypesData, isLoading: isLoadingMapPointTypes } = useGetAllMapPointTypes()
const { data: activeHousehold, isLoading: isLoadingActiveHousehold } = useGetActiveHousehold({
  query: {
    retry: 0,
    throwOnError: false,
    enabled: authStore.isAuthenticated,
  },
})
const { data: eventPointsData, isLoading: isLoadingEventPoints } = useGetAllEvents()

const { initMap, addMarkers, clearMarkers, filterMarkers } = useMap()

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
    const mapMarkers = mapPointsToMarkers(mapPointsData.value, mapPointTypesData.value)
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
    getMeetingPoints(activeHousehold.value.id).then((meetingPoints) => {
      const meetingPointsMarkers = meetingPoints
        .map((meetingPoint) => createMeetingPointMarker(meetingPoint))
        .filter((marker) => marker !== null)
      addMarkers(meetingPointsMarkers)
    })
  }

  // User location marker
  const userMarker = await createUserMarker()
  addMarkers([userMarker])

  // Subscribe to WebSocket events
  webSocket.subscribe<EventResponse>('/topic/events', (_event) => {
    // Update existing event
    clearMarkers()
    renderNewMapPoints()
  })

  webSocket.subscribe<EventResponse>('/topic/events/new', (event) => {
    // Add new event
    if (event) {
      const eventMarkers = createEventMarkers([event])
      addMarkers(eventMarkers)
    }
  })

  webSocket.subscribe<number>('/topic/events/delete', (_eventId) => {
    // Remove event
    clearMarkers()
    renderNewMapPoints()
  })
}

// Don't forget to unsubscribe on component unmount
onUnmounted(() => {
  webSocket.unsubscribe('/topic/events')
  webSocket.unsubscribe('/topic/events/new')
  webSocket.unsubscribe('/topic/events/delete')
})

onMounted(() => {
  initMap('map', () => {
    if (!isDataLoading.value) {
      renderNewMapPoints()
    } else {
      watch(isDataLoading, renderNewMapPoints)
    }
  })
})

const props = defineProps<{
  showLegend: boolean
}>()
</script>

<template>
  <div id="map" class="w-full h-full z-[1] overflow-hidden"></div>
  <NewMapLegend v-if="props.showLegend" :filter-markers="filterMarkers" />
</template>
