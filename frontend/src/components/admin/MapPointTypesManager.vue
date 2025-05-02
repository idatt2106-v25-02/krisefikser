<script setup lang="ts">
import { ref } from 'vue'
import type { MapPointType } from '@/api/generated/model'
import { useCreateMapPointType, useGetAllMapPointTypes, useUpdateMapPointType, useDeleteMapPointType } from '@/api/generated/map-point-type/map-point-type'
import { useGetAllMapPoints } from '@/api/generated/map-point/map-point'
import { useAuthStore } from '@/stores/useAuthStore'
import MapPointTypeForm from './MapPointTypeForm.vue'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

const authStore = useAuthStore()
const { data: mapPointTypes, refetch: refetchMapPointTypes } = useGetAllMapPointTypes<MapPointType[]>()
const { refetch: refetchMapPoints } = useGetAllMapPoints()

const newMapPointType = ref({
  title: '',
  description: '',
  iconUrl: '',
  openingTime: '',
})
const editingMapPointType = ref<MapPointType | null>(null)
const isDialogOpen = ref(false)

const { mutate: createMapPointType } = useCreateMapPointType({
  mutation: {
    onSuccess: () => {
      refetchMapPointTypes()

    },
  },
})
const { mutate: updateMapPointType } = useUpdateMapPointType({
  mutation: {
    onSuccess: () => {
      refetchMapPointTypes()
      refetchMapPoints()
    },
  },
})
const { mutate: deleteMapPointType } = useDeleteMapPointType({
  mutation: {
    onSuccess: () => {
      refetchMapPointTypes()
    },
  },
})

async function handleAddMapPointType() {
  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to create a map point type')
    return
  }

  if (!newMapPointType.value.title) {
    console.error('Title is required')
    return
  }

  try {
    createMapPointType({
      data: newMapPointType.value,
    })
    refetchMapPointTypes()
    newMapPointType.value = {
      title: '',
      description: '',
      iconUrl: '',
      openingTime: '',
    }
  } catch (error) {
    console.error('Error creating map point type:', error)
  }
}

async function handleDeleteMapPointType(id: number) {
  try {
    deleteMapPointType({ id })
  } catch (error) {
    console.error('Error deleting map point type:', error)
  }
}

async function handleUpdateMapPointType() {
  if (!authStore.isAdmin) {
    console.error('User must have ADMIN role to update a map point type')
    return
  }

  if (!editingMapPointType.value?.id || !editingMapPointType.value.title) {
    console.error('ID and title are required')
    return
  }

  try {
    updateMapPointType({
      id: editingMapPointType.value.id,
      data: {
        title: editingMapPointType.value.title,
        description: editingMapPointType.value.description,
        iconUrl: '/icons/' + editingMapPointType.value.iconUrl + '.svg',
        openingTime: editingMapPointType.value.openingTime,
      },
    })
    refetchMapPointTypes()
    editingMapPointType.value = null
    isDialogOpen.value = false
  } catch (error) {
    console.error('Error updating map point type:', error)
  }
}

function handleEditClick(type: MapPointType | undefined) {
  if (!type) return
  editingMapPointType.value = { ...type }
  isDialogOpen.value = true
}

function handleDialogCancel() {
  editingMapPointType.value = null
  isDialogOpen.value = false
}
</script>

<template>
  <div class="space-y-4">
    <!-- Add Form -->
    <MapPointTypeForm
      v-model="newMapPointType"
      title="Legg til ny kartpunkttype"
      @submit="handleAddMapPointType"
      @cancel="newMapPointType = {
        title: '',
        description: '',
        iconUrl: '',
        openingTime: '',
      }"
    />

    <!-- List of Map Point Types -->
    <div class="mt-6 space-y-4">
      <h3 class="text-lg font-semibold">Eksisterende kartpunkttyper</h3>
      <div v-for="type in mapPointTypes" :key="type?.id" class="border rounded-lg p-4">
        <div class="flex justify-between items-start">
          <div>
            <h4 class="font-medium">{{ type?.title || 'Ukjent tittel' }}</h4>
            <p class="text-sm text-gray-600">{{ type?.description || 'Ingen beskrivelse' }}</p>
          </div>
          <div class="flex space-x-2">
            <button
              @click="handleEditClick(type)"
              class="text-primary hover:text-primary/80"
            >
              Rediger
            </button>
            <button
              v-if="type?.id"
              @click="handleDeleteMapPointType(type.id)"
              class="text-red-500 hover:text-red-600"
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
          <DialogTitle>Rediger kartpunkttype</DialogTitle>
        </DialogHeader>
        <MapPointTypeForm
          v-if="editingMapPointType"
          v-model="editingMapPointType"
          title=""
          @submit="handleUpdateMapPointType"
          @cancel="handleDialogCancel"
        />
      </DialogContent>
    </Dialog>
  </div>
</template>
