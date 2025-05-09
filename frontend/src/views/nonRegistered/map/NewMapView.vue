<script setup lang="ts">
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { createUserMarker } from '@/components/map/marker'
import loadMapPoints from '@/components/map/marker/mapPoints'
import NewMapComponent from '@/components/map/NewMapComponent.vue'
import { useMap } from '@/components/map/useMap'
import type { Map as LeafletMap } from 'leaflet'
import { watch } from 'vue'

const { data: newMapPointsData } = useGetAllMapPoints()
const { data: newMapPointTypesData } = useGetAllMapPointTypes()

const {
  initMap: newInitMap,
  addMarkers: newAddMarkers,
  onMapCreated: newOnMapCreated,
  clearMarkers: newClearMarkers,
} = useMap()

const renderNewMapPoints = async () => {
  newClearMarkers()

  const stopDataLoadWatcher = watch(
    [newMapPointsData, newMapPointTypesData],
    ([newMapPoints, newMapPointTypes]) => {
      if (newMapPoints && newMapPointTypes) {
        console.log('Map points size', newMapPoints.length)
        const mapPoints = loadMapPoints(newMapPoints, newMapPointTypes)
        newAddMarkers(mapPoints)
        stopDataLoadWatcher()
      }
    },
    { immediate: true },
  )

  const userMarker = await createUserMarker()
  newAddMarkers([userMarker])
}

newOnMapCreated(async (_mapInstance: LeafletMap) => {
  renderNewMapPoints()
})
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
    <div class="relative flex-grow overflow-hidden">
      <NewMapComponent :init-map="newInitMap" />
    </div>
  </div>
</template>
