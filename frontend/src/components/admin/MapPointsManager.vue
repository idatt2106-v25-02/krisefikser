<script setup lang="ts">
import { ref } from 'vue'
import type { MapPoint, MapPointType } from '@/api/generated/model'
import { useCreateMapPoint, useGetAllMapPoints, useUpdateMapPoint, useDeleteMapPoint } from '@/api/generated/map-point/map-point'
import { useGetAllMapPointTypes } from '@/api/generated/map-point-type/map-point-type'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const { data: mapPoints, refetch: refetchMapPoints } = useGetAllMapPoints()
const { data: mapPointTypes } = useGetAllMapPointTypes()

const newMapPoint = ref<Partial<MapPoint>>({
  latitude: 63.4305,
  longitude: 10.3951,
  type: undefined,
})
const editingMapPoint = ref<MapPoint | null>(null)

const { mutate: createMapPoint } = useCreateMapPoint()
const { mutate: updateMapPoint } = useUpdateMapPoint()
const { mutate: deleteMapPoint } = useDeleteMapPoint()

const emit = defineEmits<{
  (e: 'map-click', lat: number, lng: number): void
}>()

function handleMapClick(lat: number, lng: number) {
  newMapPoint.value.latitude = lat
  newMapPoint.value.longitude = lng
  emit('map-click', lat, lng)
}

async function handleAddMapPoint() {
  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to create a map point')
    return
  }

  if (!newMapPoint.value.latitude || !newMapPoint.value.longitude || !newMapPoint.value.type?.id) {
    console.error('Missing required map point fields')
    return
  }

  try {
    await createMapPoint({
      data: {
        latitude: newMapPoint.value.latitude,
        longitude: newMapPoint.value.longitude,
        type: { id: newMapPoint.value.type.id },
      },
    })
    refetchMapPoints()
    newMapPoint.value = {
      latitude: 63.4305,
      longitude: 10.3951,
      type: undefined,
    }
  } catch (error) {
    console.error('Error creating map point:', error)
  }
}

async function handleUpdateMapPoint() {
  if (!editingMapPoint.value?.id) return

  try {
    await updateMapPoint({
      id: editingMapPoint.value.id,
      data: editingMapPoint.value,
    })
    refetchMapPoints()
    editingMapPoint.value = null
  } catch (error) {
    console.error('Error updating map point:', error)
  }
}

async function handleDeleteMapPoint(id: number) {
  try {
    await deleteMapPoint({ id })
    refetchMapPoints()
  } catch (error) {
    console.error('Error deleting map point:', error)
  }
}

function getMapPointTypeTitle(type: MapPointType | undefined): string {
  return type?.title || 'Ukjent type'
}
</script>

<template>
  <div class="space-y-4">
    <div class="space-y-2">
      <label class="text-sm font-medium">Kartpunkttype</label>
      <select
        v-model="newMapPoint.type"
        class="w-full px-3 py-2 border rounded-lg"
      >
        <option :value="undefined">Velg type</option>
        <option
          v-for="type in mapPointTypes"
          :key="type?.id"
          :value="type?.id ? { id: type?.id } : undefined"
        >
          {{ type?.title || 'Ukjent tittel' }}
        </option>
      </select>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Plassering</label>
      <div class="flex space-x-2">
        <input
          :value="newMapPoint.latitude?.toFixed(4)"
          readonly
          type="text"
          class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
          placeholder="Breddegrad"
        />
        <input
          :value="newMapPoint.longitude?.toFixed(4)"
          readonly
          type="text"
          class="w-1/2 px-3 py-2 border rounded-lg bg-gray-50"
          placeholder="Lengdegrad"
        />
      </div>
      <p class="text-sm text-gray-500">Klikk på kartet for å velge plassering</p>
    </div>

    <button
      @click="handleAddMapPoint"
      class="w-full bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary/90"
    >
      Legg til kartpunkt
    </button>

    <!-- List of Map Points -->
    <div class="mt-6 space-y-4">
      <h3 class="text-lg font-semibold">Eksisterende kartpunkter</h3>
      <div v-for="point in mapPoints" :key="point?.id" class="border rounded-lg p-4">
        <div class="flex justify-between items-start">
          <div>
            <h4 class="font-medium">{{ getMapPointTypeTitle(point?.type) }}</h4>
            <p class="text-sm text-gray-600">
              {{ point?.latitude?.toFixed(4) }}, {{ point?.longitude?.toFixed(4) }}
            </p>
          </div>
          <div class="flex space-x-2">
            <button
              @click="editingMapPoint = point"
              class="text-primary hover:text-primary/80"
            >
              Rediger
            </button>
            <button
              v-if="point?.id"
              @click="handleDeleteMapPoint(point.id)"
              class="text-red-500 hover:text-red-600"
            >
              Slett
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
