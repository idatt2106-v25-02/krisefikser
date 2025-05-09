<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useUpdateCurrentUserLocation } from '@/api/generated/user/user'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const authStore = useAuthStore()
const intervalId = ref<number | null>(null)
const updateLocationMutation = useUpdateCurrentUserLocation({
  mutation: {
    throwOnError: false,
  },
})

const updateLocation = async () => {
  if (!authStore.isAuthenticated) {
    if (intervalId.value) {
      clearInterval(intervalId.value)
      intervalId.value = null
    }
    return
  }

  try {
    const position = await new Promise<GeolocationPosition>((resolve, reject) => {
      const timeoutId = setTimeout(() => {
        reject(new Error('Geolocation timeout'))
      }, 10000)

      navigator.geolocation.getCurrentPosition(
        (pos) => {
          clearTimeout(timeoutId)
          resolve(pos)
        },
        (err) => {
          clearTimeout(timeoutId)
          reject(err)
        },
        { timeout: 10000, maximumAge: 0 }
      )
    })

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
  if (authStore.isAuthenticated) {
    updateLocation()
    intervalId.value = window.setInterval(updateLocation, 15000)
  }
})

onUnmounted(() => {
  if (intervalId.value !== null) {
    clearInterval(intervalId.value)
    intervalId.value = null
  }
})
</script>

<template>
  <div></div>
</template>
