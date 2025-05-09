<script lang="ts" setup>
// ... existing imports ...
import { computed, ref, watch } from 'vue'
import type {
  MapPointResponse as MapPoint,
  MapPointTypeResponse as MapPointType,
} from '@/api/generated/model'
import {
  useCreateMapPoint,
  useDeleteMapPoint,
  useGetAllMapPoints,
  useUpdateMapPoint,
} from '@/api/generated/map-point/map-point'
import { useGetAllEvents } from '@/api/generated/event/event'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import MapPointForm from './MapPointForm.vue'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'

const props = defineProps<{
  mapCoordinates?: { lat: number; lng: number } | null
}>()

const authStore = useAuthStore()
const { data: mapPoints, refetch: refetchMapPoints } = useGetAllMapPoints()
const mapPointsList = computed(() => (mapPoints.value || []) as MapPoint[])
const { refetch: refetchEvents } = useGetAllEvents()

const newMapPoint = ref<Partial<MapPoint>>({
  latitude: 63.4305,
  longitude: 10.3951,
  type: undefined,
})
const editingMapPoint = ref<MapPoint | null>(null)
const isDialogOpen = ref(false)
const isMapSelectionMode = ref(false)

const { mutate: createMapPoint } = useCreateMapPoint({
  mutation: {
    onSuccess: () => {
      refetchMapPoints()
      refetchEvents()
    },
  },
})
const { mutate: updateMapPoint } = useUpdateMapPoint({
  mutation: {
    onSuccess: () => {
      refetchMapPoints()
      refetchEvents()
    },
  },
})
const { mutate: deleteMapPoint } = useDeleteMapPoint({
  mutation: {
    onSuccess: () => {
      refetchMapPoints()
      refetchEvents()
    },
  },
})

const emit = defineEmits<{
  (e: 'map-click', lat: number, lng: number): void
  (e: 'map-selection-mode-change', isActive: boolean): void
  (e: 'map-coordinates-handled'): void
}>()

function _handleMapClick(lat: number, lng: number) {
  if (!isMapSelectionMode.value) return

  if (editingMapPoint.value) {
    editingMapPoint.value.latitude = lat
    editingMapPoint.value.longitude = lng
  } else {
    newMapPoint.value.latitude = lat
    newMapPoint.value.longitude = lng
  }
  isMapSelectionMode.value = false
  emit('map-selection-mode-change', false)
}

function handleStartMapSelection() {
  isMapSelectionMode.value = true
  emit('map-selection-mode-change', true)
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
        typeId: newMapPoint.value.type.id,
      },
    })
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
  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to update a map point')
    return
  }

  if (!editingMapPoint.value?.id) {
    console.error('ID is required')
    return
  }

  try {
    await updateMapPoint({
      id: editingMapPoint.value.id,
      data: editingMapPoint.value,
    })
    editingMapPoint.value = null
    isDialogOpen.value = false
  } catch (error) {
    console.error('Error updating map point:', error)
  }
}

async function handleDeleteMapPoint(id: number) {
  try {
    await deleteMapPoint({ id })
  } catch (error) {
    console.error('Error deleting map point:', error)
  }
}

function handleEditClick(point: MapPoint | undefined) {
  if (!point) return
  editingMapPoint.value = { ...point }
  isDialogOpen.value = true
}

function handleDialogCancel() {
  editingMapPoint.value = null
  isDialogOpen.value = false
}

function getMapPointTypeTitle(type: MapPointType | undefined): string {
  return type?.title || 'Ukjent type'
}

// Watch for incoming map coordinates
watch(
  () => props.mapCoordinates,
  (coordinates) => {
    if (coordinates && isMapSelectionMode.value) {
      _handleMapClick(coordinates.lat, coordinates.lng)
      emit('map-coordinates-handled')
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="space-y-4">
    <!-- Add Form -->
    <MapPointForm
      v-model="newMapPoint"
      title="Legg til nytt kartpunkt"
      @cancel="
        newMapPoint = {
          latitude: 63.4305,
          longitude: 10.3951,
          type: undefined,
        }
      "
      @submit="handleAddMapPoint"
      @start-map-selection="handleStartMapSelection"
    />

    <!-- List of Map Points -->
    <div class="mt-6 space-y-4">
      <h3 class="text-lg font-semibold">Eksisterende kartpunkter</h3>
      <div v-for="point in mapPointsList" :key="point?.id" class="border rounded-lg p-4">
        <div class="flex justify-between items-start">
          <div>
            <h4 class="font-medium">{{ getMapPointTypeTitle(point?.type) }}</h4>
            <p class="text-sm text-gray-600">
              {{ point?.latitude?.toFixed(4) }}, {{ point?.longitude?.toFixed(4) }}
            </p>
          </div>
          <div class="flex space-x-2">
            <button class="text-primary hover:text-primary/80" @click="handleEditClick(point)">
              Rediger
            </button>
            <button
              v-if="point?.id"
              class="text-red-500 hover:text-red-600"
              @click="handleDeleteMapPoint(point.id)"
            >
              Slett
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Dialog -->
    <Dialog v-model:open="isDialogOpen">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Rediger kartpunkt</DialogTitle>
        </DialogHeader>
        <MapPointForm
          v-if="editingMapPoint"
          v-model="editingMapPoint"
          title=""
          @cancel="handleDialogCancel"
          @submit="handleUpdateMapPoint"
          @start-map-selection="handleStartMapSelection"
        />
      </DialogContent>
    </Dialog>
  </div>
</template>
