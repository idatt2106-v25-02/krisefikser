<script lang="ts" setup>
import { onUnmounted, ref, watch } from 'vue'
import type { Map as LeafletMap, Marker } from 'leaflet'
import L from 'leaflet'
import type { HouseholdMemberResponse } from '@/api/generated/model'
import { useGetActiveHousehold } from '@/api/generated/household/household'

const props = defineProps<{
  map: LeafletMap
}>()

const memberMarkers = ref<Marker[]>([])
const { data: activeHousehold } = useGetActiveHousehold()

// Create a custom icon for household members
const memberIcon = L.icon({
  iconUrl: '/images/member-marker.png', // Using a PNG file instead of SVG
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32],
})

// Update markers when household data changes
watch(
  () => activeHousehold.value?.members,
  (newMembers) => {
    // Remove existing markers
    memberMarkers.value.forEach((marker) => marker.remove())
    memberMarkers.value = []

    if (!newMembers) return

    // Add new markers for each member with location
    newMembers.forEach((member: HouseholdMemberResponse) => {
      if (member.user.latitude && member.user.longitude) {
        const marker = L.marker([member.user.latitude, member.user.longitude], { icon: memberIcon })
          .bindPopup(`${member.user.firstName} ${member.user.lastName}`)
          .addTo(props.map)

        memberMarkers.value.push(marker)
      }
    })
  },
  { immediate: true },
)

// Clean up markers when component is unmounted
onUnmounted(() => {
  memberMarkers.value.forEach((marker) => marker.remove())
})
</script>
<template>
  <div></div>
</template>
