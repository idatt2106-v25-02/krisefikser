<script lang="ts" setup>
import { onUnmounted, ref, watch } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import MapPointLayer from '@/components/map/layer/MapPointLayer.vue'
import EventLayer from '@/components/map/layer/EventLayer.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import type { EventResponse as Event, MapPointResponse as MapPoint } from '@/api/generated/model'
import L from 'leaflet'

const props = defineProps<{
  mapPoints: MapPoint[] | undefined
  events: Event[] | undefined
  selectMode?: boolean
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

// Watch for changes in select mode and update cursor
watch(
  () => props.selectMode,
  (isActive) => {
    if (mapInstance.value) {
      const container = mapInstance.value.getContainer()
      container.style.cursor = isActive ? 'crosshair' : ''
    }
  },
  { immediate: true },
)

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

    <EventLayer v-if="mapInstance && events" :events="events" :map="mapInstance as L.Map" />

    <MapLegend
      :has-active-household="false"
      :is-adding-meeting-point="false"
      :show-user-location="false"
      :user-in-crisis-zone="false"
      :user-location-available="false"
      @toggle-user-location="() => {}"
    />
  </div>
</template>
