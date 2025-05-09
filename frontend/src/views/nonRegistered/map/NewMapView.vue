<script setup lang="ts">
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { createUserMarker } from '@/components/map/marker'
import loadMapPoints from '@/components/map/marker/mapPoints'
import NewMapComponent from '@/components/map/NewMapComponent.vue'
import { useMap } from '@/components/map/useMap'
import type { Map as LeafletMap } from 'leaflet'
import { watchEffect } from 'vue'

const { data: mapPointsData } = useGetAllMapPoints()
const { data: mapPointTypesData } = useGetAllMapPointTypes()

const { initMap, addMarkers, onMapCreated } = useMap()

onMapCreated(async (_mapInstance: LeafletMap) => {
  watchEffect(() => {
    if (mapPointsData.value && mapPointTypesData.value) {
      const mapPoints = loadMapPoints(mapPointsData.value, mapPointTypesData.value)
      addMarkers(mapPoints)
    }
  })

  const userMarker = await createUserMarker()
  addMarkers([userMarker])
})
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
    <div class="relative flex-grow overflow-hidden">
      <NewMapComponent :init-map="initMap" />
    </div>
  </div>
</template>
