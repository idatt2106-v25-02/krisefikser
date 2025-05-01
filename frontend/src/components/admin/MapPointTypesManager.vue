<script setup lang="ts">
import { ref } from 'vue'
import type { MapPointType } from '@/api/generated/model'
import { useCreateMapPointType, useGetAllMapPointTypes, useUpdateMapPointType, useDeleteMapPointType } from '@/api/generated/map-point-type/map-point-type'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const { data: mapPointTypes, refetch: refetchMapPointTypes } = useGetAllMapPointTypes()

const newMapPointType = ref<Partial<MapPointType>>({
  title: '',
  description: '',
  iconUrl: '',
  openingTime: '',
})
const editingMapPointType = ref<MapPointType | null>(null)

const { mutate: createMapPointType } = useCreateMapPointType()
const { mutate: updateMapPointType } = useUpdateMapPointType()
const { mutate: deleteMapPointType } = useDeleteMapPointType()

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
    await createMapPointType({
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
    await deleteMapPointType({ id })
    refetchMapPointTypes()
  } catch (error) {
    console.error('Error deleting map point type:', error)
  }
}
</script>

<template>
  <div class="space-y-4">
    <div class="space-y-2">
      <label class="text-sm font-medium">Tittel</label>
      <input
        v-model="newMapPointType.title"
        type="text"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Tittel på kartpunkttype"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Beskrivelse</label>
      <textarea
        v-model="newMapPointType.description"
        class="w-full px-3 py-2 border rounded-lg"
        rows="3"
        placeholder="Beskriv kartpunkttypen"
      ></textarea>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Ikon URL</label>
      <input
        v-model="newMapPointType.iconUrl"
        type="text"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="URL til ikon"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Åpningstid</label>
      <input
        v-model="newMapPointType.openingTime"
        type="text"
        class="w-full px-3 py-2 border rounded-lg"
        placeholder="Åpningstid"
      />
    </div>

    <button
      @click="handleAddMapPointType"
      class="w-full bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary/90"
    >
      Legg til kartpunkttype
    </button>

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
              @click="editingMapPointType = { ...type }"
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
  </div>
</template>
