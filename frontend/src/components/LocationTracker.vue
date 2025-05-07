<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useUpdateCurrentUserLocation } from '@/api/generated/user/user'
import { toast } from 'vue-sonner'

const updateLocationMutation = useUpdateCurrentUserLocation()
let intervalId: number | null = null

const updateLocation = async () => {
  try {
    // Request location from browser
    const position = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject)
    })

    // Send location update to server
    await updateLocationMutation.mutateAsync({
      data: {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
      }
    })
  } catch (error) {
    console.error('Failed to update location:', error)
    toast.error('Kunne ikke oppdatere posisjon')
  }
}

onMounted(() => {
  // Request initial location permission and update
  updateLocation()

  // Set up interval for periodic updates (every 15 seconds)
  intervalId = window.setInterval(updateLocation, 15000)
})

onUnmounted(() => {
  // Clean up interval when component is unmounted
  if (intervalId !== null) {
    clearInterval(intervalId)
  }
})
</script>

<template>
  <!-- This component doesn't render anything visible -->
</template>
