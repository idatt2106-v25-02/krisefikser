<script setup lang="ts">
import { ref, watch } from 'vue'
import { useCreateMeetingPoint, useUpdateMeetingPoint, useDeleteMeetingPoint } from '@/api/generated/meeting-points/meeting-points'
import type { MeetingPointResponse } from '@/api/generated/model'

const props = defineProps<{
  householdId: string
  point?: MeetingPointResponse
  position?: { lat: number; lng: number }
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved'): void
}>()

const name = ref(props.point?.name || '')
const description = ref(props.point?.description || '')
const latitude = ref(props.point?.latitude || props.position?.lat || 0)
const longitude = ref(props.point?.longitude || props.position?.lng || 0)

const { mutate: createMeetingPoint } = useCreateMeetingPoint()
const { mutate: updateMeetingPoint } = useUpdateMeetingPoint()
const { mutate: deleteMeetingPoint } = useDeleteMeetingPoint()

const handleSubmit = async () => {
  const data = {
    name: name.value,
    description: description.value,
    latitude: latitude.value,
    longitude: longitude.value
  }

  if (props.point) {
    await updateMeetingPoint({
      householdId: props.householdId,
      id: props.point.id || '',
      data
    })
  } else {
    await createMeetingPoint({
      householdId: props.householdId,
      data
    })
  }

  emit('saved')
  emit('close')
}

const handleDelete = async () => {
  if (props.point) {
    await deleteMeetingPoint({
      householdId: props.householdId,
      id: props.point.id || ''
    })
    emit('close')
  }
}
</script>

<template>
  <div class="fixed top-4 right-4 bg-white p-4 rounded-lg shadow-lg w-80 z-50">
    <h2 class="text-xl font-bold mb-4">
      {{ point ? 'Rediger møteplass' : 'Ny møteplass' }}
    </h2>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700">Navn</label>
        <input
          v-model="name"
          type="text"
          required
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700">Beskrivelse</label>
        <textarea
          v-model="description"
          required
          class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
        ></textarea>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Breddegrad</label>
          <input
            v-model="latitude"
            type="number"
            step="any"
            required
            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Lengdegrad</label>
          <input
            v-model="longitude"
            type="number"
            step="any"
            required
            class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
      </div>

      <div class="flex justify-between">
        <button
          type="submit"
          class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          {{ point ? 'Oppdater' : 'Lagre' }}
        </button>

        <button
          v-if="point"
          type="button"
          @click="handleDelete"
          class="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
        >
          Slett
        </button>

        <button
          type="button"
          @click="$emit('close')"
          class="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
        >
          Avbryt
        </button>
      </div>
    </form>
  </div>
</template>
