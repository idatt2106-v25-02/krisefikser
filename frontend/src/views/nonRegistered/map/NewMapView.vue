<script setup lang="ts">
import NewMapComponent from '@/components/map/NewMapComponent.vue'
import { useMap } from '@/components/map/useMap'
import type { Map as LeafletMap } from 'leaflet'

const { initMap, addMarker, onMapCreated } = useMap()

const markers_icons = {
  shelter: '/icons/map/shelter.svg',
  user: '/icons/map/user.svg',
  home: '/icons/map/home.svg',
  household_members: '/icons/map/household-members.svg',
  meeting_point: '/icons/map/meeting-point.svg',
}

function addUserLocationMarker() {
  addMarker(63.4305, 10.3951, markers_icons.user, 'User Location')
}

onMapCreated((_mapInstance: LeafletMap) => {
  addShelterMarkers()
  addUserLocationMarker()
})
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
    <div class="relative flex-grow overflow-hidden">
      <NewMapComponent :init-map="initMap" />
    </div>
  </div>
</template>
