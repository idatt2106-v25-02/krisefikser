<script setup lang="ts">
import { createUserMarker } from '@/components/map/marker'
import NewMapComponent from '@/components/map/NewMapComponent.vue'
import { useMap } from '@/components/map/useMap'
import type { Map as LeafletMap } from 'leaflet'

const { initMap, addMarker, onMapCreated } = useMap()

function addUserLocationMarker() {
  addMarker(createUserMarker())
}

onMapCreated((_mapInstance: LeafletMap) => {
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
