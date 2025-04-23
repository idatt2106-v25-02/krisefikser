<script setup lang="ts">
import { ref } from 'vue'
import MapComponent from '@/components/map/MapComponent.vue'
import ShelterLayer from '@/components/map/ShelterLayer.vue'
import CrisisLayer from '@/components/map/CrisisLayer.vue'
import UserLocationLayer from '@/components/map/UserLocationLayer.vue'
import MapLegend from '@/components/map/MapLegend.vue'
import { shelters, crisisAreas } from '@/components/map/mapData'
import type { CrisisArea } from '@/components/map/mapData'
import L from 'leaflet'

// Map and related refs
const mapRef = ref<InstanceType<typeof MapComponent> | null>(null)
const userLocationRef = ref<InstanceType<typeof UserLocationLayer> | null>(null)
const mapInstance = ref<L.Map | null>(null)
const userLocationAvailable = ref(false)
const showUserLocation = ref(false)
const userInCrisisZone = ref(false)
const currentCrisisArea = ref<CrisisArea | null>(null)

// Handle map instance being set
function onMapCreated(map: L.Map) {
  mapInstance.value = map
}

// Handle user location toggle
function toggleUserLocation(show: boolean) {
  showUserLocation.value = show
  if (userLocationRef.value) {
    userLocationRef.value.toggleUserLocation(show)
  }
}

// Handle user entering/leaving crisis zone
function handleUserCrisisZoneChange(inZone: boolean, area?: CrisisArea) {
  userInCrisisZone.value = inZone
  currentCrisisArea.value = area || null
}

// Get user location availability status
function onUserLocationStatus(available: boolean) {
  userLocationAvailable.value = available
}
</script>

<template>
  <div class="relative w-full h-screen">
    <MapComponent ref="mapRef" @map-created="onMapCreated" />

    <ShelterLayer :map="mapInstance" :shelters="shelters" />

    <CrisisLayer :map="mapInstance" :crisis-areas="crisisAreas" />

    <UserLocationLayer
      ref="userLocationRef"
      :map="mapInstance"
      :crisis-areas="crisisAreas"
      @user-in-crisis-zone="handleUserCrisisZoneChange"
      @user-location-available="onUserLocationStatus"
    />

    <MapLegend
      :user-location-available="userLocationAvailable"
      :show-user-location="showUserLocation"
      :user-in-crisis-zone="userInCrisisZone"
      @toggle-user-location="toggleUserLocation"
    />
  </div>
</template>

<style scoped>
/* No styles needed as we're using Tailwind classes */
</style>
