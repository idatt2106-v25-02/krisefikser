<script setup lang="ts">
import { ref } from 'vue'
import { useCreateMeetingPoint, useUpdateMeetingPoint, useDeleteMeetingPoint, useGetMeetingPoints } from '@/api/generated/meeting-points/meeting-points.ts'
import type { MeetingPointResponse } from '@/api/generated/model'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'

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

const { refetch: refetchMeetingPoints } = useGetMeetingPoints(props.householdId)
const { mutate: createMeetingPoint } = useCreateMeetingPoint({
  mutation: {
    onSuccess: () => {
      refetchMeetingPoints()
    }
  }
})
const { mutate: updateMeetingPoint } = useUpdateMeetingPoint({
  mutation: {
    onSuccess: () => {
      refetchMeetingPoints()
    }
  }
})
const { mutate: deleteMeetingPoint } = useDeleteMeetingPoint({
  mutation: {
    onSuccess: () => {
      refetchMeetingPoints()
    }
  }
})

const handleSubmit = async () => {
  const data = {
    name: name.value,
    description: description.value,
    latitude: latitude.value,
    longitude: longitude.value
  }

  if (props.point) {
    updateMeetingPoint({
      householdId: props.householdId,
      id: props.point.id || '',
      data
    })
  } else {
    createMeetingPoint({
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
   <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700">Navn</label>
        <Input
          v-model="name"
          type="text"
          required
          class="mt-1"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700">Beskrivelse</label>
        <Input
          v-model="description"
          required
          class="mt-1"
        />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Breddegrad</label>
          <Input
            v-model="latitude"
            type="number"
            step="any"
            required
            class="mt-1"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Lengdegrad</label>
          <Input
            v-model="longitude"
            type="number"
            step="any"
            required
            class="mt-1"
          />
        </div>
      </div>

      <div class="flex justify-between">
        <Button type="submit">
          {{ point ? 'Oppdater' : 'Lagre' }}
        </Button>

        <Button
          v-if="point"
          type="button"
          variant="destructive"
          @click="handleDelete"
        >
          Slett
        </Button>

        <Button
          type="button"
          variant="outline"
          @click="$emit('close')"
        >
          Avbryt
        </Button>
      </div>
    </form>

</template>
