<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import MapPointLayer from '@/components/map/MapPointLayer.vue'
import EventLayer from '@/components/map/EventLayer.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import type { MapPoint, Event } from '@/api/generated/model'
import L from 'leaflet'

defineProps<{
  mapPoints: MapPoint[] | undefined
  events: Event[] | undefined
}>()

const emit = defineEmits<(e: 'map-click', lat: number, lng: number) => void>()

const mapRef = ref<InstanceType<typeof MapComponent> | null>(null)
const mapInstance = ref<L.Map | null>(null)

function onMapCreated(map: L.Map) {
  mapInstance.value = map
  map.on('click', (e: L.LeafletMouseEvent) => {
    const { lat, lng } = e.latlng
    emit('map-click', lat, lng)
  })
}

onUnmounted(() => {
  if (mapInstance.value) {
    mapInstance.value.off('click')
  }
})
</script>

<template>
  <div class="flex-1 relative">
    <MapComponent ref="mapRef" @map-created="onMapCreated" />

    <MapPointLayer
      v-if="mapInstance && mapPoints"
      :map="mapInstance as L.Map"
      :map-points="mapPoints"
    />

    <EventLayer
      v-if="mapInstance && events"
      :map="mapInstance as L.Map"
      :events="events"
    />

    <MapLegend
      :user-location-available="false"
      :show-user-location="false"
      :user-in-crisis-zone="false"
      :is-adding-meeting-point="false"
      :has-active-household="false"
      @toggle-user-location="() => {}"
    />
  </div>
</template>
