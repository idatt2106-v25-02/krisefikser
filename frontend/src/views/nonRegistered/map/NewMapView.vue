<script setup lang="ts">
import { onMounted } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import { useMap } from '@/components/map/useMap'

const { map, addMarker } = useMap()

function addShelterMarkers() {
  addMarker(63.4305, 10.3951, '/icons/map/shelter.svg', 'Shelter')
}

onMounted(() => {
  addShelterMarkers()
})
</script>

<template>
  <div>
    <MapComponent :map="map" />
  </div>
</template>
