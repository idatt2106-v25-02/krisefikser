<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useUpdateCurrentUserLocation } from '@/api/generated/user/user'
import { useAuthStore } from '@/stores/auth/useAuthStore.ts'

const authStore = useAuthStore()
const updateLocationMutation = useUpdateCurrentUserLocation({
  mutation: {
    throwOnError: false,
  },
})
let intervalId: number | null = null

const updateLocation = async () => {
  // Only update location if user is authenticated
  if (!authStore.isAuthenticated) {
    return
  }

  try {
    // Request location from browser
    const position = await new Promise<GeolocationPosition>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject)
    })

    // Send location update to server
    await updateLocationMutation.mutateAsync({
      data: {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      },
    })
  } catch (error) {
    console.error('Failed to update location:', error)
  }
}

onMounted(() => {
  // Only start tracking if user is authenticated
  if (authStore.isAuthenticated) {
    // Request initial location permission and update
    updateLocation()

    // Set up interval for periodic updates (every 15 seconds)
    intervalId = window.setInterval(updateLocation, 15000)
  }
})

onUnmounted(() => {
  // Clean up interval when component is unmounted
  if (intervalId !== null) {
    clearInterval(intervalId)
  }
})
</script>
