<script setup lang="ts">
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { createUserMarker } from '@/components/map/marker'
import loadMapPoints from '@/components/map/marker/mapPoints'
import NewMapComponent from '@/components/map/NewMapComponent.vue'
import { useMap } from '@/components/map/useMap'
import type { Map as LeafletMap } from 'leaflet'
import { watch } from 'vue'

const { data: mapPointsData } = useGetAllMapPoints()
const { data: mapPointTypesData } = useGetAllMapPointTypes()

const { initMap, addMarkers, onMapCreated, clearMarkers } = useMap()

const renderNewMapPoints = async () => {
  clearMarkers()

  const stopDataLoadWatcher = watch(
    [mapPointsData, mapPointTypesData],
    ([newMapPoints, newMapPointTypes]) => {
      if (newMapPoints && newMapPointTypes) {
        console.log('Map points size', newMapPoints.length)
        const mapPoints = loadMapPoints(newMapPoints, newMapPointTypes)
        addMarkers(mapPoints)
        stopDataLoadWatcher()
      }
    },
    { immediate: true },
  )

  const userMarker = await createUserMarker()
  addMarkers([userMarker])
}

onMapCreated(async (_mapInstance: LeafletMap) => {
  renderNewMapPoints()
})
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
    <div class="relative flex-grow overflow-hidden">
      <NewMapComponent :init-map="initMap" />
    </div>
  </div>
</template>
